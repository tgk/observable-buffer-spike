(defproject observable-buffer "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1909"]
                 [ragge/core.async "0.1.0-SNAPSHOT"]]
  :source-paths ["src/clj"]
  :plugins [[lein-cljsbuild "0.3.3"]]
  :cljsbuild {:builds
              [{:notify-command ["terminal-notifier" "-title" "lein-cljsbuild" "-message"]
                :source-paths ["src/cljs"]
                :compiler {;; :libs ["src/js"]
                           :output-to "observable_buffer.js"
                           :pretty-print true
                           :optimizations :whitespace}}]})
