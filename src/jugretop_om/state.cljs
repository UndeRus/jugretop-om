(ns ^:figwheel-always jugretop-om.state)

(def app-state (atom {
    :page 1
    :posts []
}))
