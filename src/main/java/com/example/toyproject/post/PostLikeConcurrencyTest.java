package com.example.toyproject.post;


import com.example.toyproject.postlike.PostLikeService;
import com.example.toyproject.repository.PostLikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// 동시성 테스트 클래스
// 테스트 목적 : JUnit으로 “동시에 100번 like 호출 → DB에는 1건만 저장”**을 증명
@SpringBootTest
public class PostLikeConcurrencyTest {
    /**
            * 동시성 테스트 목적:
            * - 같은 postId + 같은 userId로 동시에 like 요청이 몰려도
            * - DB에는 (post_id, user_id) row가 1건만 생성되는지 검증
            *
         * 전제:
             * - PostgreSQL
             * - UNIQUE(post_id, user_id)
             * - INSERT ... ON CONFLICT DO NOTHING (insertIgnoreDuplicate)
     * */


        @Autowired
        private PostLikeService postLikeService;

        @Autowired
        private PostLikeRepository postLikeRepository;

        // 테스트용 값 (실제 존재하는 postId를 넣는 게 안전)
        private final Long postId = 1L;
        private final String userId = "concurrency-test-user";

        @BeforeEach
        void setUp() {
            // 테스트 시작 전에 해당 (postId, userId) 좋아요가 남아있다면 제거
            postLikeRepository.deleteByPostIdAndUserId(postId, userId);
        }

        @Test
        void 동시에_100번_like_요청해도_DB에는_1건만_저장된다() throws Exception {

            int threadCount = 100;

            // 고정 스레드 풀
            ExecutorService executor = Executors.newFixedThreadPool(20);

            // 모든 스레드가 준비될 때까지 기다렸다가 동시에 출발시키기 위한 래치
            CountDownLatch ready = new CountDownLatch(threadCount);
            CountDownLatch start = new CountDownLatch(1);

            // 모든 작업이 끝날 때까지 기다리기 위한 래치
            CountDownLatch done = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        ready.countDown();     // "나 준비됨"
                        start.await();         // 출발 신호 대기

                        // ✅ 동시에 like 호출
                        postLikeService.like(postId, userId);

                    } catch (Exception e) {
                        // 테스트는 "예외가 발생하지 않아야" 더 좋지만
                        // 혹시 모를 상황을 보기 위해 로그 남길 수 있음
                        e.printStackTrace();
                    } finally {
                        done.countDown();      // "나 끝남"
                    }
                });
            }

            // 모든 스레드 준비 완료까지 대기
            ready.await();

            // 동시에 출발!
            start.countDown();

            // 전부 종료 대기
            done.await();

            executor.shutdown();

            // 1  - int 타입
            // 1L - long 타입
            // 검증 1: 해당 유저의 해당 글 좋아요 row는 1건이어야 함
            boolean exists = postLikeRepository.existsByPostIdAndUserId(postId, userId);
            assertTrue(exists);

            // 검증 2: 좋아요 개수는 1이어야 함 (테스트 전 삭제했기 때문에)
            long count = postLikeRepository.countByPostIdAndUserId(postId, userId);
            assertEquals(1L, count);
        }
}
