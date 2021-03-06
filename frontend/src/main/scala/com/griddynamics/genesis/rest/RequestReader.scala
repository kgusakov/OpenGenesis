package com.griddynamics.genesis.rest

import javax.servlet.http.HttpServletRequest

import net.liftweb.json._

object RequestReader {
    def read[B](request: HttpServletRequest) (block: Map[String, Any] => B) = {
        val paramsMap: Map[String, Any] = GenesisRestController.extractParamsMap(request)
        block(paramsMap)
    }

}
