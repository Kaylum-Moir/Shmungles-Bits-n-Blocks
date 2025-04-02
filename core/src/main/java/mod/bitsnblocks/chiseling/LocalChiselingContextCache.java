package mod.bitsnblocks.chiseling;

import mod.bitsnblocks.api.chiseling.ChiselingOperation;
import mod.bitsnblocks.api.chiseling.IChiselingContext;
import mod.bitsnblocks.api.chiseling.ILocalChiselingContextCache;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;

public class LocalChiselingContextCache implements ILocalChiselingContextCache
{
    private static final LocalChiselingContextCache INSTANCE = new LocalChiselingContextCache();

    public static LocalChiselingContextCache getInstance()
    {
        return INSTANCE;
    }

    private final EnumMap<ChiselingOperation, IChiselingContext> contexts = new EnumMap<>(ChiselingOperation.class);

    private LocalChiselingContextCache()
    {
    }

    @Override
    public Optional<IChiselingContext> get(ChiselingOperation operation)
    {
        return Optional.ofNullable(contexts.get(operation));
    }

    @Override
    public void set(ChiselingOperation operation, final IChiselingContext context)
    {
        this.contexts.put(operation, context);
    }

    @Override
    public void clear(ChiselingOperation operation)
    {
        this.contexts.remove(operation);
    }

    public void clearCache()
    {
        Arrays.stream(ChiselingOperation.values()).forEach(this::clear);
    }
}
