(ns cnrepl.helpers
  {:author "Chas Emerick"}
  (:require
   [cnrepl.middleware.load-file :as load-file])
  (:import
   (System.IO FileInfo )))                          ;;; (java.io File StringReader)

(defn load-file-command
  "(If it is available, sending nrepl.middleware.load-file
    compatible messages is far preferable.)

   Returns a string expression that can be sent to an nREPL session to
   load the Clojure code in given local file in the remote REPL's environment,
   preserving debug information (e.g. line numbers, etc).

   Typical usage: (nrepl-client-fn
                    {:op \"eval\" :code
                      (load-file-command \"/path/to/clojure/file.clj\")})

   If appropriate, the source path from which the code is being loaded may
   be provided as well (suitably trimming the file's path to a relative one
   when loaded).

   The 3-arg variation of this function expects the full source of the file to be loaded,
   the source-root-relative path of the source file, and the name of the file.  e.g.:

     (load-file-command \"…code here…\" \"some/ns/name/file.clj\" \"file.clj\")"
  ([f] (load-file-command f nil))
  ([f source-root]
   (let [^String abspath (if (string? f) f (.DirectoryName ^FileInfo f))                             ;;; .getAbsolutePath  ^File
         source-root (cond
                       (nil? source-root) ""
                       (string? source-root) source-root
                       (instance? FileInfo source-root) (.DirectoryName ^FileInfo source-root))]     ;;;  File .getAbsolutePath  ^File
     (load-file-command (slurp abspath :encoding "UTF-8")
                        (if (and (seq source-root)
                                 (.StartsWith abspath source-root))                                  ;;;  .startsWith
                          (-> abspath
                              (.Substring (count source-root))                                       ;;; 
                              (System.Text.RegularExpressions.Regex/Replace "^[/\\\\]" ""))           ;;; (.replaceAll "^[/\\\\]" "")
                          abspath)
                        (-> abspath FileInfo. .Name))))                                               ;;; File. .getName
  ([code file-path file-name]
   (load-file/load-file-code code file-path file-name)))