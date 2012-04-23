/*
 * Copyright (c) 2010-2012 Grid Dynamics Consulting Services, Inc, All Rights Reserved
 *   http://www.griddynamics.com
 *
 *   This library is free software; you can redistribute it and/or modify it under the terms of
 *   the GNU Lesser General Public License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or any later version.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *   AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *   DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 *   FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *   DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *   SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *   CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *   OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *   @Project:     Genesis
 *   @Description: Execution Workflow Engine
 */

package com.griddynamics.genesis.service.impl

import com.griddynamics.genesis.service
import com.griddynamics.genesis.api
import api.RequestResult
import collection.JavaConversions.asScalaIterator
import org.springframework.transaction.annotation.Transactional
import org.apache.commons.configuration.Configuration

// TODO: add synchronization?
class DefaultConfigService(val config: Configuration, val writeConfig: Configuration, val configRO: Configuration) extends service.ConfigService {

    @Transactional(readOnly = true)
    def get[B](name: String, default: B): B = {
      (default match {
        case value: Int => config.getInt(name, value)
        case value: Long => config.getLong(name, value)
        case value: String => config.getString(name, value)
        case value: Boolean => config.getBoolean(name, value)
        case _ => throw new IllegalArgumentException("Not supported type")
      }).asInstanceOf[B]
    }

    @Transactional(readOnly = true)
    def get(name: String) = Option(config.getProperty(name))

    import service.GenesisSystemProperties.PREFIX_DB
    @Transactional(readOnly = true)
    def listSettings(prefix: Option[String]) = prefix.map(config.getKeys(_)).getOrElse(config.getKeys())
        .map(k => api.ConfigProperty(k, config.getString(k), k.startsWith(PREFIX_DB) || configRO.containsKey(k))).toSeq.sortBy(_.name)

    @Transactional
    def update(name: String, value: Any) {writeConfig.setProperty(name, value)}

    @Transactional
    def delete(key: String) = RequestResult(isSuccess = try {
        writeConfig.clearProperty(key)
        true
    } catch {
        case _ => false
    })
}