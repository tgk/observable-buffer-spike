(ns observable-buffer.core
  (:require [clojure.core.async :refer [go <!! >!! sliding-buffer chan]]
            [clojure.core.async.impl.protocols :as impl])
    (:import [java.util LinkedList Queue]))

;; Safer, redefined buffer

(deftype FixedBuffer [^LinkedList buf ^long n]
  impl/Buffer
  (full? [this]
    (= (.size buf) n))
  (remove! [this]
    (.removeLast buf))
  (add! [this itm]
    (assert (not (impl/full? this)) "Can't add to a full buffer")
    (.addFirst buf itm))
  clojure.lang.Counted
  (count [this]
    (.size buf))
  clojure.lang.IDeref
  (deref [this]
    (seq buf)))

(defn fixed-buffer [^long n]
  (FixedBuffer. (LinkedList.) n))

;; Alternative method

(defprotocol TransparentBuffer
  (inspect [this]))

(extend-type FixedBuffer
  TransparentBuffer
  (inspect [this]
    (seq (.buf this))))

;; Example usage

(comment

  (def buf (fixed-buffer 2))

  (def example-chan (chan buf))

  (>!! example-chan :foo)
  (>!! example-chan :bar)

  (<!! example-chan)

  (count buf)
  @buf
  (inspect buf))

;; To be accessible from ClojureScript, there should also be a cljs
;; folder with implementations for those
