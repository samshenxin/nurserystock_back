package jdy.zsf.nurserystock.utils;

import kd.bos.audit.Audit;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.TypesContainer;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectUtils {

            private static final ConcurrentHashMap<String, Object> serviceObjectMap = new ConcurrentHashMap<>();
            private static final ConcurrentHashMap<String, Method> serviceMethodMap = new ConcurrentHashMap<>();

            public static Object invokeCosmicMethod(String appServiceFacory, String serviceName, String methodName, Object... paras) {
                Class<?> factory = TypesContainer.getOrRegister(appServiceFacory);
                Object result = null;
                Object serviceObject = serviceObjectMap.get(serviceName);
                if (serviceObject == null) {
                    synchronized (serviceObjectMap) {
                        serviceObject = serviceObjectMap.get(serviceName);
                        if (serviceObject == null) {
                            try {
                                serviceObject = factory.getMethod("getService", String.class).invoke(null, serviceName);
                            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                            if (serviceObject != null) {
                                serviceObjectMap.put(serviceName, serviceObject);
                            }
                        }
                    }
                }

                if (serviceObject != null) {
                    Method serviceMethod = findServiceMethod(serviceObject.getClass(), methodName, paras == null ? 0 : paras.length);
                    Audit audit = RequestContext.get().getAudit();
                    if (audit.getServiceName() == null) {
                        audit.setServiceName(serviceName + '.' + methodName);
                    }

                    try {
                        result = serviceMethod.invoke(serviceObject, paras);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }

                return result;
            }

            private static Method findServiceMethod(Class<?> clazz, String method, int paramterLength) {
                String key = clazz.getName() + '#' + method + '#' + paramterLength;
                Method serviceMethod = serviceMethodMap.get(key);
                if (serviceMethod == null) {
                    synchronized (serviceMethodMap) {
                        serviceMethod = serviceMethodMap.get(key);
                        if (serviceMethod == null) {
                            Method[] methods = clazz.getMethods();
                            for (Method m : methods) {
                                if (m.getName().equalsIgnoreCase(method) && m.getParameterCount() == paramterLength) {
                                    serviceMethod = m;
                                    serviceMethodMap.put(key, m);
                                    break;
                                }
                            }
                        }
                    }

                    if (serviceMethod == null) {
                        throw new KDException(new ErrorCode("###", ResManager.loadKDString("未发现类%s的方法%s", "DispatchServiceImpl_1", "bos-mservice-form")), clazz.getName(), method);
                    }
                }
                return serviceMethod;
            }
        }

