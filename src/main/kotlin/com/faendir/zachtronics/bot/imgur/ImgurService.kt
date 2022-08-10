/*
 * Copyright (c) 2022
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.zachtronics.bot.imgur

import com.faendir.zachtronics.bot.config.ImgurProperties
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import java.io.File

@Service
class ImgurService(private val imgurProperties: ImgurProperties) {

    private val restTemplate = RestTemplate()

    private var token: String? = null

    private fun login(): String {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        headers["User-Agent"] = "Mozilla/5.0 (X11; Linux x86_64; rv:103.0) Gecko/20100101 Firefox/103.0"

        val body = LinkedMultiValueMap<String, Any>()
        body.add("username", imgurProperties.username)
        body.add("password", imgurProperties.password)
        body.add("remember", "remember")
        body.add("submit", "")
        val response = restTemplate.exchange("https://imgur.com/signin", HttpMethod.POST, HttpEntity(body, headers), String::class.java)

        return response.headers["set-cookie"]
            ?.find { it.startsWith("accesstoken") }
            ?.let { cookie -> Regex("accesstoken=([^;]*);.*").matchEntire(cookie)?.groupValues?.get(1) }
            ?: throw RuntimeException("Imgur login failed")
    }

    fun upload(gif: ByteArray): String {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        headers["User-Agent"] = "Mozilla/5.0 (X11; Linux x86_64; rv:103.0) Gecko/20100101 Firefox/103.0"
        headers["Authorization"] = "Bearer ${login()}"
        val body = LinkedMultiValueMap<String, Any>()
        body.add("image", ByteArrayResource(gif))

        if (token != null) {
            try {
                return doUpload(body, headers)
            } catch (e: HttpStatusCodeException) {
                if (e.statusCode != HttpStatus.UNAUTHORIZED) {
                    logger.warn(
                        "Imgur returned error.\nHeaders:\n${e.responseHeaders?.toList()?.joinToString { "${it.first}=${it.second.joinToString()}" }}\n",
                        e
                    )
                    throw RuntimeException("Imgur returned error: ${e.message}")
                }
            }
        }
        token = login()
        try {
            return doUpload(body, headers)
        } catch (e: HttpStatusCodeException) {
            logger.warn("Imgur returned error.\nHeaders:\n${e.responseHeaders?.toList()?.joinToString { "${it.first}=${it.second.joinToString()}" }}\n", e)
            throw RuntimeException("Imgur returned error: ${e.message}")
        }
    }

    private fun doUpload(body: LinkedMultiValueMap<String, Any>, headers: HttpHeaders): String {
        val result = restTemplate.exchange("https://api.imgur.com/3/image", HttpMethod.POST, HttpEntity(body, headers), Response::class.java)
        return if (result.statusCode == HttpStatus.OK) {
            val link = result.body?.data?.link
            if (link != null) {
                if (link.endsWith(".")) link + "mp4"
                else link
            } else {
                throw RuntimeException("Imgur did not return a link: ${result.body}")
            }
        } else {
            throw RuntimeException("Failed to upload gif to imgur: ${result.body}")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ImgurService::class.java)
    }
}

@Serializable
private data class Response(val data: Data?)

@Serializable
private data class Data(val link: String)