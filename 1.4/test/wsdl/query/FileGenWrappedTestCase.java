/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.wsdl.query;

import test.AxisFileGenTestBase;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * This tests to make sure the wrapper element types for a wrapped
 * document/literal service are not generated by WSDL2Java.
 *
 * @author Tom Jordahl (tomj@macromedia.com)
 */
public class FileGenWrappedTestCase extends AxisFileGenTestBase {
    public FileGenWrappedTestCase(String name) {
        super(name);
    }

    /**
     * List of files which should be generated.
     */
    protected Set shouldExist() {
        HashSet set = new HashSet();
        set.add("QueryBean.java");
        set.add("QueryTest.java");
        set.add("QueryTestSoapBindingImpl.java");
        set.add("QueryTestSoapBindingStub.java");
        set.add("QueryTestService.java");
        set.add("QueryTestServiceLocator.java");
        set.add("QueryTestServiceTestCase.java");
        set.add("FileGenWrappedTestCase.java");
        set.add("QueryTest.wsdl");
        set.add("deploy.wsdd");
        set.add("undeploy.wsdd");
        return set;
    } // shouldExist

    /**
     * List of files which may be generated.
     */
    protected Set mayExist() {
        HashSet set = new HashSet();
        // none
        return set;
    } // shouldExist

    /**
     * The directory containing the files that should exist.
     */
    protected String rootDir() {
        return "build" + File.separator + "work" + File.separator +
                "test" + File.separator + "wsdl" + File.separator +
                "query";
    } // rootDir

}

