package net.chesstango.sosa.master.configs;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Mauricio Coria
 */
@Slf4j
public class WorkerScope implements Scope {
    private Map<String, WorkerScopeImp> scopes
            = Collections.synchronizedMap(new HashMap<>());

    private final static ThreadLocal<String> threadWorkerScope = new ThreadLocal<>();

    @Override
    public Object get(@NonNull String name, @NonNull ObjectFactory<?> objectFactory) {
        WorkerScopeImp scope = scopes.computeIfAbsent(getConversationId(), WorkerScope::createScope);
        return scope.get(name, objectFactory);
    }

    @Nullable
    public Object remove(@NonNull String name) {
        WorkerScopeImp scope = scopes.get(getConversationId());
        return scope.remove(name);
    }

    @Override
    public void registerDestructionCallback(@NonNull String name, @NonNull Runnable callback) {
        WorkerScopeImp scope = scopes.get(getConversationId());
        scope.registerDestructionCallback(name, callback);
    }

    @Nullable
    public Object resolveContextualObject(@NonNull String key) {
        WorkerScopeImp scope = scopes.get(getConversationId());
        return scope.resolveContextualObject(key);
    }

    @Override
    public String getConversationId() {
        return getThreadConversationId();
    }

    public static void setThreadConversationId(String id) {
        threadWorkerScope.set(id);
    }

    public static String getThreadConversationId() {
        return threadWorkerScope.get();
    }

    public static void unsetThreadConversationId() {
        threadWorkerScope.remove();
    }

    private static WorkerScopeImp createScope(String workerId) {
        return new WorkerScopeImp(workerId);
    }


    private static class WorkerScopeImp implements Scope {
        private final String workerId;

        private final Map<String, Object> scopedObjects
                = Collections.synchronizedMap(new HashMap<>());

        private final Map<String, Runnable> destructionCallbacks
                = Collections.synchronizedMap(new HashMap<>());

        private WorkerScopeImp(String workerId) {
            this.workerId = workerId;
            log.info("WorkerScope created: {}", workerId);
        }

        @Override
        public Object get(@NonNull String name, @NonNull ObjectFactory<?> objectFactory) {
            if (!scopedObjects.containsKey(name)) {
                scopedObjects.put(name, objectFactory.getObject());
            }
            return scopedObjects.get(name);
        }

        @Nullable
        public Object remove(@NonNull String name) {
            destructionCallbacks.remove(name);
            return scopedObjects.remove(name);
        }

        @Override
        public void registerDestructionCallback(@NonNull String name, @NonNull Runnable callback) {
            destructionCallbacks.put(name, callback);
        }

        @Nullable
        public Object resolveContextualObject(@NonNull String key) {
            return null;
        }

        @Override
        public String getConversationId() {
            return workerId;
        }
    }
}
