/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.cn.identity.internal.util;

import org.apache.fineract.cn.lang.ServiceException;

import java.util.UUID;

import static org.apache.fineract.cn.identity.internal.util.IdentityConstants.UUID_IDENTIFIER;

public class ConvertIdentifier {


    // TODO meter este metodo en un validador generico
    public static UUID convertToUUID(String identifier) {
        try {
            return UUID.fromString(identifier);
        } catch (IllegalArgumentException ex) {
            throw ServiceException.badRequest(String.format("Error during converting %s [%s]" , UUID_IDENTIFIER, identifier));
        }
    }

    public static String convertToString(UUID identifier) {
        return identifier.toString();
    }
}
