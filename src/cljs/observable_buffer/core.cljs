(ns observable-buffer.core
  (:require-macros [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :refer [<! >! chan]]
            [cljs.core.async.impl.protocols :as impl]
            [cljs.core.async.impl.buffers :as bufs]))

(defn ring-buffer-seq
  [ring-buffer]
  (let [arr (make-array (alength (.-arr ring-buffer)))]
    (doseq [idx (range (alength (.-arr ring-buffer)))]
      (aset arr idx
            (aget (.-arr ring-buffer)
                  (js-mod
                   (- (.-head ring-buffer) idx 1)
                   (alength (.-arr ring-buffer))))))
    (take (.-length ring-buffer)
          (js->clj arr))))

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
