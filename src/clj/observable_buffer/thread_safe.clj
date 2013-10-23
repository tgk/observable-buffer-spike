(ns observable-buffer.thread-safe
  (:require [clojure.core.async :refer [go <!! >!! chan]]
            [clojure.core.async.impl.protocols :as impl]))

(defprotocol TransparentBuffer
  (inspect [this]))

(deftype TransparentFixedBuffer [buf-ref n]
  impl/Buffer
  (full? [this]
    (= (count @buf-ref) n))
  (remove! [this]
    (dosync
     (let [[h & t] @buf-ref]
       (ref-set buf-ref (vec t))
       h)))
  (add! [this itm]
    (dosync
     (assert (not (impl/full? this)) "Can't add to a full buffer")
     (alter buf-ref conj itm)))
  clojure.lang.Counted
  (count [this]
    (count @buf-ref))
  TransparentBuffer
  (inspect [this]
    (deref buf-ref)))

(defn transparent-fixed-buffer [^long n]
  (TransparentFixedBuffer. (ref []) n))

;; Example usage

(comment

  (def buf (transparent-fixed-buffer 2))

  (def example-chan (chan buf))

  (>!! example-chan :foo)
  (>!! example-chan :bar)

  (<!! example-chan)

  (count buf)
  (inspect buf))
