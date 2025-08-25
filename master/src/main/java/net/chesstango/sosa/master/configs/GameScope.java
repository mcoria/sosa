package net.chesstango.sosa.master.configs;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@Slf4j
public class GameScope implements Scope {
    private Map<String, GameScopeImp> scopes
            = Collections.synchronizedMap(new HashMap<>());

    private final static ThreadLocal<String> threadGameScope = new ThreadLocal<>();

    @Override
    public Object get(@NonNull String name, @NonNull ObjectFactory<?> objectFactory) {
        GameScopeImp scope = scopes.computeIfAbsent(getConversationId(), GameScope::createScope);
        return scope.get(name, objectFactory);
    }

    @Nullable
    public Object remove(@NonNull String name) {
        GameScopeImp scope = scopes.get(getConversationId());
        return scope.remove(name);
    }

    @Override
    public void registerDestructionCallback(@NonNull String name, @NonNull Runnable callback) {
        GameScopeImp scope = scopes.get(getConversationId());
        scope.registerDestructionCallback(name, callback);
    }

    @Nullable
    public Object resolveContextualObject(@NonNull String key) {
        GameScopeImp scope = scopes.get(getConversationId());
        return scope.resolveContextualObject(key);
    }

    @Override
    public String getConversationId() {
        return getThreadConversationId();
    }

    public static void setThreadConversationId(String id) {
        threadGameScope.set(id);
    }

    public static String getThreadConversationId() {
        return threadGameScope.get();
    }

    public static void unsetThreadConversationId() {
        threadGameScope.remove();
    }

    private static GameScopeImp createScope(String gameId) {
        log.info("[{}] Creating GameScopeImp", gameId);
        return new GameScopeImp(gameId);
    }


    private static class GameScopeImp implements Scope {
        private final String gameId;

        private final Map<String, Object> scopedObjects
                = Collections.synchronizedMap(new HashMap<>());

        private final Map<String, Runnable> destructionCallbacks
                = Collections.synchronizedMap(new HashMap<>());

        private GameScopeImp(String gameId) {
            this.gameId = gameId;
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
            return gameId;
        }
    }
}
