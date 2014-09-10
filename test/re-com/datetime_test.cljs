(ns re-comp-test.datetime-test
  (:require-macros [cemerick.cljs.test :refer (is are deftest with-test run-tests testing test-var)])
  (:require [cemerick.cljs.test]
            [reagent.core :as reagent]
            [re-com.datetime :as datetime]))

(def default-min [0 0])
(def default-max [23 59])
(def alternate-min [6 0])
(def alternate-max [21 59])

(deftest test-first-char
  (are [expected actual] (= expected actual)
    "1"   (datetime/first-char "1" default-min default-max)   ;; Valid character - should be returned unchanged
    ""    (datetime/first-char "a" default-min default-max)    ;; Expected invalid character to be ignored.
    "08" (datetime/first-char "8" default-min default-max)  ;; Number exceeds default mimimum - should be treated as second digit of hour.
    ""   (datetime/first-char "5" alternate-min alternate-max)))   ;; Invalid character - outside time range

(deftest test-second-char
  (are [expected actual] (= expected actual)
    "11"   (datetime/second-char "11" default-min default-max)   ;; Valid character - should be returned unchanged
    "01"   (datetime/second-char "01" default-min default-max)   ;; Valid character - should be returned unchanged
    "1"    (datetime/second-char "1a" default-min default-max)   ;; Expected invalid character to be ignored.
    "2"    (datetime/second-char "25" default-min default-max)   ;; Number exceeds default mimimum - second digit should be ignored.
    "0"   (datetime/second-char  "05" alternate-min alternate-max)   ;; Invalid character - outside time range
    "06"   (datetime/second-char  "06" alternate-min alternate-max)   ;; Within time range
    "2"   (datetime/second-char  "22" alternate-min alternate-max))) ;; Outside range

(deftest test-third-char
  (are [expected actual] (= expected actual)
    "11:"   (datetime/third-char "11:" default-min default-max)   ;; Valid character - should be returned unchanged except for added colon
    "11:"   (datetime/third-char "11-" default-min default-max)   ;; Valid character - should be changed to a colon except for added colon
    "06:3"  (datetime/third-char "063" default-min default-max)   ;; Valid character - should be returned unchanged except for added colon
    "11:"   (datetime/third-char "11a" default-min default-max)   ;; Expected invalid character to be ignored and colon added.
    "23:"   (datetime/third-char "236" default-min default-max))) ;; Number exceeds number of minutes that are possible - third digit should be ignored and colon added.

(deftest test-fourth-char
  (are [expected actual] (= expected actual)
    "11:3"   (datetime/fourth-char "11:3" default-min default-max)   ;; Valid character - should be returned unchanged
    "01:0"   (datetime/fourth-char "01:0" default-min default-max)   ;; Valid character - should be returned unchanged
    "11:"    (datetime/fourth-char "11::" default-min default-max)   ;; Expected additional colon to be ignored.
    "11:"    (datetime/fourth-char "11:a" default-min default-max)   ;; Expected invalid character to be ignored.
    "22:"    (datetime/fourth-char "22:6" default-min default-max))) ;; Number exceeds number of minutes that are possible - third digit should be ignored and colon added.

(deftest test-fifth-char
  (are [expected actual] (= expected actual)
    "11:30"   (datetime/fifth-char "11:30" default-min default-max)   ;; Valid character - should be returned unchanged
    "01:00"   (datetime/fifth-char "01:00" default-min default-max)   ;; Valid character - should be returned unchanged
    "11:0"    (datetime/fifth-char "11:0a" default-min default-max)))   ;; Expected invalid character to be ignored.

(deftest test-validate-hours
  (are [expected actual] (= expected actual)
    false (datetime/validate-hours "aa" default-min default-max)   ;; Invalid character - not valid
    true (datetime/validate-hours "01" default-min default-max)
    true (datetime/validate-hours "22" default-min default-max)
    true (datetime/validate-hours "18" alternate-min alternate-max)
    true (datetime/validate-hours "25" alternate-min alternate-max))) ;; Even though it is after the max

(deftest test-validate-third-char
  (are [expected actual] (= expected actual)
    false (datetime/validate-third-char \a default-min default-max)   ;; Not valid
    false (datetime/validate-third-char \- default-min default-max)   ;; Not valid
    true  (datetime/validate-third-char \: default-min default-max)))

(deftest test-validate-minutes
  (are [expected actual] (= expected actual)
    false (datetime/validate-minutes "aa" default-min default-max)   ;; Invalid character - not valid
    true (datetime/validate-minutes "55" default-min default-max)
    true (datetime/validate-minutes "00" alternate-min alternate-max)
    false (datetime/validate-minutes "65" alternate-min alternate-max))) ;; After max minutes in an hour

(deftest test-is-valid
  (are [expected actual] (= expected actual)
    false (datetime/is-valid (reagent/atom nil) default-min default-max)
    false (datetime/is-valid (reagent/atom "") default-min default-max)
    false (datetime/is-valid (reagent/atom "1") default-min default-max)
    false (datetime/is-valid (reagent/atom "04") default-min default-max)
    false (datetime/is-valid (reagent/atom "04:") default-min default-max)
    false (datetime/is-valid (reagent/atom "04:3") default-min default-max)
    true  (datetime/is-valid (reagent/atom "04:30") default-min default-max)
    false (datetime/is-valid (reagent/atom "24:30") default-min default-max)       ;; After max
    true  (datetime/is-valid (reagent/atom "14:30") alternate-min alternate-max)
    false (datetime/is-valid (reagent/atom "04:30") alternate-min alternate-max)   ;; Before min
    false (datetime/is-valid (reagent/atom "23:15") alternate-min alternate-max))) ;; After max