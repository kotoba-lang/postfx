(ns postfx-test
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.test :refer [deftest is]]
            [kotoba.compiler.core :as compiler]
            [kotoba.compiler.ir :as ir]))

(def source (slurp "src/postfx.kotoba"))
(defn map-value [document key]
  (second (some #(when (= key (first %)) %) (second document))))
(defn effect-count [pipeline]
  (count (second (map-value pipeline :effects))))

(deftest reference-preserves-constructors-pipelines-and-layout-data
  (let [kir (:kir (compiler/compile-source source :js-kotoba-v1))
        execute #(ir/execute kir %1 %2)
        config ["map" [[:intensity ["f64" 0.3]]
                         [:radius ["f64" 4.0]]
                         [:threshold ["f64" 0.8]]]]
        effect (execute 'bloom [config])
        pipeline (execute 'add [(execute 'new-pipeline []) effect])]
    (is (= ["keyword" :bloom] (map-value effect :type)))
    (is (= ["f64" 0.8] (map-value effect :threshold)))
    (is (= 1 (effect-count pipeline)))
    (is (= 3 (effect-count (execute 'nintendo []))))
    (is (= 2 (effect-count (execute 'retro []))))
    (is (= 10 (effect-count (execute 'final-fantasy []))))
    (is (= 6 (effect-count (execute 'baminiku-character []))))
    (is (= ["bool" true] (map-value pipeline :enabled)))
    (is (= ["f64" 0.0]
           (map-value (execute 'bloom-params [config]) :_pad)))
    (is (= #{} (set (:effects kir))))))

(defn compiler-root []
  (nth (iterate #(.getParent ^java.nio.file.Path %)
                (java.nio.file.Path/of (.toURI (io/resource "kotoba/compiler/core.clj")))) 4))
(defn base64 [value] (.encodeToString (java.util.Base64/getEncoder) value))

(deftest restricted-javascript-and-typed-wasm-preserve-observable-presets
  (let [javascript (compiler/compile-source source :js-kotoba-v1)
        wasm (compiler/compile-source source :wasm32-browser-kotoba-v1)
        js64 (base64 (.getBytes ^String (:source javascript) "UTF-8"))
        wasm64 (base64 ^bytes (:bytes wasm))
        probe (shell/sh
               "node" "--input-type=module" "-e"
               (str "import(process.argv[1]).then(async host=>{"
                    "const j=await import('data:text/javascript;base64," js64 "');"
                    "const w=await host.instantiateKotoba(Buffer.from(process.argv[2],'base64'));"
                    "const get=(d,k)=>d[1].find(e=>e[0]===k)[1],count=p=>get(p,':effects')[1].length;"
                    "for(const x of [j.instantiateKotoba({}),w.instance.exports]){"
                    "if(count(x.nintendo())!==3||count(x.retro())!==2||count(x['final-fantasy']())!==10||count(x['baminiku-character']())!==6)throw Error('preset');"
                    "if(get(x.nintendo(),':enabled')[1]!==true)throw Error('enabled')}"
                    "}).catch(e=>{console.error(e);process.exit(99)})")
               (.toString (.toUri (.resolve (compiler-root) "runtime/browser-host.mjs"))) wasm64)]
    (is (zero? (:exit probe)) (:err probe))))

(deftest production-source-authority
  (is (= ["src/postfx.kotoba"]
         (->> (file-seq (io/file "src")) (filter #(.isFile %)) (map str) sort vec))))
