/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.weid.constant;

/**
 * @author marsli
 */
public enum ChainVersion {

    // fisco v2.x.x
    FISCO_V2(2),
    // fisco v3.x.x
    FISCO_V3(3);

    private int version;

    ChainVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public static boolean contains(int version) {
        return FISCO_V2.version == version || FISCO_V3.version == version;
    }

}
