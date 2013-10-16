(ns observable-buffer.core
  (:require-macros [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :refer [<! >! chan]]
            [cljs.core.async.impl.protocols :as impl]
            [cljs.core.async.impl.buffers :as bufs]))

(defn empty-ring-buffer
  [ring-buffer]
  (loop [elms nil]
    (if (> (.-length ring-buffer) 0)
      (recur (cons (.pop ring-buffer) elms))
      elms)))

(defn fill-ring-buffer
  [ring-buffer elms]
  (doseq [e elms]
    (.unshift ring-buffer e)))

(defn ring-buffer-seq
  [ring-buffer]
  (let [elms (empty-ring-buffer ring-buffer)]
    (fill-ring-buffer ring-buffer (reverse elms))
    elms))

(defprotocol TransparentBuffer
  (inspect [this]))

(extend-type bufs/FixedBuffer
  TransparentBuffer
  (inspect [this]
    (ring-buffer-seq (.-buf this))))

(extend-type bufs/DroppingBuffer
  TransparentBuffer
  (inspect [this]
    (ring-buffer-seq (.-buf this))))

(extend-type bufs/SlidingBuffer
  TransparentBuffer
  (inspect [this]
    (ring-buffer-seq (.-buf this))))

(defn pprint
  [& args]
  (.log js/console (apply pr-str args)))

(defn ^:export demo1
  []
  (.log js/console "Demo 1")

  (.log js/console "Seq-ing ring-buffers")
  (let [ring-buffer (bufs/ring-buffer 3)]
    (.unshift ring-buffer :foo)
    (.unshift ring-buffer :bar)
    (pprint (ring-buffer-seq ring-buffer))
    (.pop ring-buffer)
    (pprint (ring-buffer-seq ring-buffer)))

  (let [b (bufs/fixed-buffer 3)
        c (chan b)]
    (go (.log js/console "Inspecting fixed buffer")
        (>! c 1)
        (>! c 2)
        (pprint (inspect b))
        (<! c)
        (pprint (inspect b))))

  (let [b (bufs/dropping-buffer 3)
        c (chan b)]
    (go (.log js/console "Inspecting dropping buffer")
        (>! c 1)
        (>! c 2)
        (>! c 3)
        (>! c 4)
        (>! c 5)
        (>! c 6)
        (pprint (inspect b))
        (<! c)
        (pprint (inspect b))))

  (let [b (bufs/sliding-buffer 3)
        c (chan b)]
    (go (.log js/console "Inspecting sliding buffer")
        (>! c 1)
        (>! c 2)
        (>! c 3)
        (>! c 4)
        (>! c 5)
        (>! c 6)
        (pprint (inspect b))
        (<! c)
        (pprint (inspect b)))))
