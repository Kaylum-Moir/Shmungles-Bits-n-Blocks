package mod.bitsnblocks.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import mod.bitsnblocks.BitsNBlocks;
import mod.bitsnblocks.api.config.ICommonConfiguration;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

final class MultiThreadAwareStorageEngine<TPayload> implements IMultiThreadedStorageEngine<TPayload>
{
    private static ExecutorService saveService;

    private static synchronized void ensureThreadPoolSetup() {
        if (saveService == null) {
            final ClassLoader classLoader = BitsNBlocks.class.getClassLoader();
            final AtomicInteger genericThreadCounter = new AtomicInteger();
            saveService = Executors.newFixedThreadPool(
              ICommonConfiguration.getInstance().getBlockSaveThreadCount().get(),
              runnable -> {
                  final Thread thread = new Thread(runnable);
                  thread.setContextClassLoader(classLoader);
                  thread.setName(String.format("Chisels and Bits Block save handler #%s", genericThreadCounter.incrementAndGet()));
                  thread.setDaemon(true);
                  return thread;
              }
            );
        }
    }

    private final Codec<TPayload> internalEngine;

    MultiThreadAwareStorageEngine(final Codec<TPayload> internalEngine) {
        this.internalEngine = internalEngine;
    }

    @Override
    public CompletableFuture<Tag> encodeAsync(TPayload payload, HolderLookup.Provider provider) {
        ensureThreadPoolSetup();
        return CompletableFuture.supplyAsync(() -> {
            final NbtOps ops = NbtOps.INSTANCE;
            final RegistryOps<Tag> registryOps = RegistryOps.create(ops, provider);
            return internalEngine.encodeStart(registryOps, payload).getPartialOrThrow((s) -> new IllegalStateException("Failed to encode payload: " + s));
        }, saveService);
    }

    @Override
    public CompletableFuture<TPayload> decodeAsync(Tag tag, HolderLookup.Provider provider) {
        ensureThreadPoolSetup();
        return CompletableFuture.supplyAsync(() -> {
            final NbtOps ops = NbtOps.INSTANCE;
            final RegistryOps<Tag> registryOps = RegistryOps.create(ops, provider);
            final Dynamic<Tag> dynamic = new Dynamic<>(registryOps, tag);
            return internalEngine.parse(dynamic).getPartialOrThrow((s) -> new IllegalStateException("Failed to decode payload: " + s));
        }, saveService);
    }
}
