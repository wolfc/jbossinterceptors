/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
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

package org.jboss.interceptor.proxy;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;
import org.jboss.interceptor.InterceptorException;
import org.jboss.interceptor.proxy.javassist.CompositeHandler;
import org.jboss.interceptor.model.InterceptionModel;
import org.jboss.interceptor.model.registry.InterceptionModelRegistry;
import org.jboss.interceptor.model.metadata.InterceptorMetadata;
import org.jboss.interceptor.util.InterceptionUtils;
import sun.reflect.ReflectionFactory;

/**
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public class InterceptorProxyCreatorImpl implements InterceptorProxyCreator
{

   private List<InterceptionModelRegistry<Class<?>, ?>> interceptionModelRegistries;

   private List<InterceptionHandlerFactory<?>> interceptionHandlerFactories;

   public InterceptorProxyCreatorImpl(List<InterceptionModelRegistry<Class<?>, ?>> interceptionModelRegistries, List<InterceptionHandlerFactory<?>> interceptionHandlerFactories)
   {
      this.interceptionModelRegistries = interceptionModelRegistries;
      this.interceptionHandlerFactories = interceptionHandlerFactories;
   }

   public InterceptorProxyCreatorImpl(InterceptionModelRegistry<Class<?>, ?> interceptionModelRegistries, InterceptionHandlerFactory<?> interceptionHandlerFactories)
   {
      this.interceptionModelRegistries = Collections.<InterceptionModelRegistry<Class<?>, ?>>singletonList(interceptionModelRegistries);
      this.interceptionHandlerFactories = Collections.<InterceptionHandlerFactory<?>>singletonList(interceptionHandlerFactories);
   }


   public <T> T createProxyFromInstance(final Object target, Class<T> proxifiedClass, Class<?>[] constructorTypes, Object[] constructorArguments, InterceptorMetadata interceptorClassMetadata)
   {
       MethodHandler interceptorMethodHandler = createMethodHandler(target, proxifiedClass, interceptorClassMetadata);
      return createProxyInstance(InterceptionUtils.createProxyClassWithHandler(proxifiedClass, interceptorMethodHandler), interceptorMethodHandler);
   }

   public <T> T createProxyFromClass(Class<T> proxifiedClass, Class<?>[] constructorTypes, Object[] constructorArguments, InterceptorMetadata interceptorClassMetadata)
   {
      T instance = createAdvisedSubclassInstance(proxifiedClass, constructorTypes, constructorArguments);
      MethodHandler interceptorMethodHandler = createSubclassingMethodHandler(instance, proxifiedClass, interceptorClassMetadata);
      ((ProxyObject)instance).setHandler(new CompositeHandler(Arrays
            .asList(new MethodHandler[]{interceptorMethodHandler})));
      return instance;
   }

   public <T> T createAdvisedSubclassInstance(Class<T> proxifiedClass, Class<?>[] constructorParameterTypes, Object[] constructorArguments)
   {
       try
       {
           Class<T> clazz = InterceptionUtils.createProxyClass(proxifiedClass, true);
           Constructor<T> constructor = clazz.getConstructor(constructorParameterTypes);
           return constructor.newInstance(constructorArguments);
       }
       catch (Exception e)
       {
           throw new InterceptorException(e);
       }
   }

   public <T> T createProxyInstance(Class<T> proxyClass, MethodHandler interceptorMethodHandler)
   {
      Constructor<T> constructor = null;
      try
      {
         constructor = getNoArgConstructor(proxyClass);
         if (constructor == null)
         {
            constructor = getReflectionFactoryConstructor(proxyClass);
         }
      }
      catch (Exception e)
      {
         throw new InterceptorException(e);
      }
      if (constructor == null)
         throw new InterceptorException("Cannot found a constructor for the proxy class: " + proxyClass + ". " +
               "No no-arg constructor is available, and sun.reflect.ReflectionFactory is not accessible");
      try
      {
         T proxyObject = constructor.newInstance();
         if (interceptorMethodHandler != null)
         {
            ((ProxyObject) proxyObject).setHandler(interceptorMethodHandler);
         }
         return proxyObject;
      }
      catch (Exception e)
      {
         throw new InterceptorException(e);
      }
   }

   public <T> MethodHandler createMethodHandler(Object target, Class<T> proxyClass, InterceptorMetadata interceptorMetadata)
   {
      return new InterceptorMethodHandler(target, proxyClass, getModelsFor(proxyClass), interceptionHandlerFactories, interceptorMetadata);
   }

    public <T> MethodHandler createSubclassingMethodHandler(Object targetInstance, Class<T> proxyClass, InterceptorMetadata interceptorMetadata)
    {
       return new SubclassingInterceptorMethodHandler(targetInstance, getModelsFor(proxyClass), interceptionHandlerFactories, interceptorMetadata);
    }


   private <T> List<InterceptionModel<Class<?>, ?>> getModelsFor(Class<T> proxyClass)
   {
      List<InterceptionModel<Class<?>, ?>> interceptionModels = new ArrayList<InterceptionModel<Class<?>, ?>>();
      for (InterceptionModelRegistry interceptionModelRegistry : interceptionModelRegistries)
      {
         interceptionModels.add(interceptionModelRegistry.getInterceptionModel(proxyClass));
      }
      return interceptionModels;
   }

   private <T> Constructor<T> getNoArgConstructor(Class<T> clazz)
   {
      Constructor<T> constructor;
      try
      {
        constructor = clazz.getConstructor(new Class[]{});
      }
      catch (NoSuchMethodException e)
      {
         return null;
      }
      return constructor;
   }

   private <T> Constructor<T> getReflectionFactoryConstructor(Class<T> proxyClass)
         throws NoSuchMethodException
   {
      try
      {
         Constructor<T> constructor;
         ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();
         constructor = reflectionFactory.newConstructorForSerialization(proxyClass, Object.class.getDeclaredConstructor());
         return constructor;
      }
      catch (NoSuchMethodException e)
      {
         return null;
      }
      catch (SecurityException e)
      {
         return null;
      }
   }
}


