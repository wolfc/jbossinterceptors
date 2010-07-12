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

package org.jboss.interceptor.reader;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.jboss.interceptor.spi.metadata.ClassMetadata;
import org.jboss.interceptor.spi.metadata.MethodMetadata;

/**
 * @author Marius Bogoevici
 */
public class ReflectiveMethodMetadata implements MethodMetadata, Serializable
{
   private Method javaMethod;

   private ReflectiveMethodMetadata(Method method)
   {
      this.javaMethod = method;
   }

   public static MethodMetadata of(Method method)
   {
      return new ReflectiveMethodMetadata(method);
   }

   public Annotation getAnnotation(Class<? extends Annotation> annotationClass)
   {
      return javaMethod.getAnnotation(annotationClass);
   }

   public Method getJavaMethod()
   {
      return javaMethod;
   }

   public ClassMetadata getReturnType()
   {
      return ReflectiveClassMetadata.of(javaMethod.getReturnType());
   }

}