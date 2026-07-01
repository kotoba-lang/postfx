(ns postfx-test
  (:require [clojure.test :refer [deftest is testing]]
            [postfx]))
(deftest namespace-loads
  (testing "the restored CLJC namespace loads"
    (is (some? postfx))))
