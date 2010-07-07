/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.interceptor.model.metadata;

import java.util.List;

import org.jboss.interceptor.model.InterceptionType;
import org.jboss.interceptor.model.metadata.reader.ClassMetadataProvider;
import org.jboss.interceptor.model.metadata.reader.MethodMetadataProvider;

/**
 * A 
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public interface InterceptorMetadata
{
   ClassMetadataProvider getInterceptorClass();

   /**
    * Returns the list of method references to be invoked on this class when doing
    * interception (as an interceptor is supposed to invoke the superclass
    * methods too)
    *
    * @param interceptionType
    * @return a list of methods
    */
   List<MethodMetadataProvider> getInterceptorMethods(InterceptionType interceptionType);

   boolean isInterceptor();

   boolean isTargetClass();
}