(ns postfx-test
  "Ported 1:1 from `kami-postfx/src/lib.rs`'s `#[cfg(test)] mod tests` (lib.rs:308-332),
  plus a namespace-load smoke test."
  (:require [clojure.test :refer [deftest is testing]]
            [postfx]))

(deftest namespace-loads
  (testing "the restored CLJC namespace loads"
    (is (some? (the-ns 'postfx)))))

;; Rust: test_nintendo_preset (lib.rs:312-317)
(deftest test-nintendo-preset
  (let [p (postfx/nintendo)]
    (is (= 3 (count (:effects p))))
    (is (:enabled p))))

;; Rust: test_final_fantasy_preset (lib.rs:319-324)
(deftest test-final-fantasy-preset
  (let [p (postfx/final-fantasy)]
    (is (= 10 (count (:effects p))))
    (is (:enabled p))))

;; Rust: test_baminiku_character_preset (lib.rs:326-331)
(deftest test-baminiku-character-preset
  (let [p (postfx/baminiku-character)]
    (is (= 6 (count (:effects p))))
    (is (:enabled p))))
