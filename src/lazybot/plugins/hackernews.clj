(ns lazybot.plugins.hackernews
  (:use [lazybot registry utilities]
        [clojure.data.json :only [read-json]])
  (:require [clj-http.client :as http]))

(def *search-url*
  "http://api.thriftdb.com/api.hnsearch.com/items/_search")

(defn search
  "searches HN for a given term and optional limitation"
  [term & limit]
  (let [raw-results
    (http/get *search-url*
              {:query-params
                { "q" term
                  "limit" (or (first limit) 5)}})]
    (:results (read-json (:body raw-results)))))

(defn handle-results
  "parses JSON response from HN search"
  [results]
  (map
    (fn [result]
      (select-keys (:item result) [:title :url :points]))
    results))

(defn stringify-result
  [result]
  (str (:title result) ": (" (:points result) "pts) " (:url result)))

(defn print-results
  "takes a handler function and a seq of result
   maps calling the printer on each result"
  [results handler]
  (doseq [result results]
    (handler (stringify-result result))))

(defn println-results
  [results]
  (print-results results println))

(defplugin
  (:cmd
   "Returns top results for a given term on HN"
   #{"hackernews hn"}
   (fn [{:keys [bot channel args] :as com-m}]
     (let [[term & limit] args
           stories (handle-results search)
           handler (partial send-message com-m)]
      (print-results handler stories)))))
