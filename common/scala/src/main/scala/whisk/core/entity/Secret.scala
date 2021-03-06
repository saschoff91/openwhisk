/*
 * Copyright 2015-2016 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package whisk.core.entity

import scala.util.Try
import javax.crypto.KeyGenerator
import java.util.Base64
import spray.json.JsValue
import spray.json.RootJsonFormat
import spray.json.JsString
import spray.json.deserializationError

/**
 * Secret, a cryptographic string such as a key used for authentication.
 *
 * It is a value type (hence == is .equals, immutable and cannot be assigned null).
 * The constructor is private so that argument requirements are checked and normalized
 * before creating a new instance.
 *
 * @param key the secret key, required not null or empty
 */
protected[core] class Secret private (val key: String) extends AnyVal {
    def apply() = key
    protected[entity] def toJson = JsString(toString)
    override def toString = key
}

protected[core] object Secret extends ArgNormalizer[Secret] {
    /** Minimum secret length */
    private val MIN_LENGTH = 64

    /**
     * Creates a Secret from a string. The string must be a valid secret already.
     *
     * @param str the secret as string, at least 64 characters
     * @return Secret instance
     * @throws IllegalArgumentException is argument is not a valid Secret
     */
    @throws[IllegalArgumentException]
    override protected[entity] def factory(str: String): Secret = {
        require(str.length >= MIN_LENGTH, s"secret must be at least $MIN_LENGTH characters")
        new Secret(str)
    }

    /**
     * Creates a new random secret.
     *
     * @return Secret
     */
    protected[core] def apply(): Secret = {
        Secret(rand.alphanumeric.take(MIN_LENGTH).mkString)
    }

    implicit val serdes = new RootJsonFormat[Secret] {
        def write(s: Secret) = s.toJson

        def read(value: JsValue) = Try {
            val JsString(s) = value
            Secret(s)
        } getOrElse deserializationError("secret malformed")
    }

    private val rand = new scala.util.Random(new java.security.SecureRandom())
}
