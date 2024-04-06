package sd_projeto;

import java.util.BitSet;
import java.util.List;
import java.util.function.Function;

/**
 * Implementação de um Bloom Filter Thread-Safe em Java.
 * Esta implementação é adaptada do código original para ser Thread-Safe.
 * <p>
 * <a href="https://medium.com/javarevisited/java-how-to-implement-a-bloom-filter-and-what-is-it-used-for-070632cbe7a9">SOURCE</a>
 * @author Oliver Foster
 * @param <T> O tipo de elemento armazenado no Bloom Filter.
 */
public class BloomFilter<T> {
    private BitSet bitSet;
    private int size;
    private List<Function<T, Integer>> hashFunctions;


    /**
     * Construtor para criar um Bloom Filter Thread-Safe.
     * @param size O tamanho do filtro de Bloom.
     * @param hashFunctions As funções de hash a serem usadas para calcular as posições no filtro de Bloom.
     */
    public BloomFilter(int size, List<Function<T, Integer>> hashFunctions) {
        this.size = size;
        this.bitSet = new BitSet(size);
        this.hashFunctions = hashFunctions;
    }

    /**
     * Adiciona um elemento ao Bloom Filter.
     * @param element O elemento a ser adicionado.
     */
    public synchronized void add(T element) {
        for (Function<T, Integer> hashFunction : hashFunctions) {
            int hash = Math.abs(hashFunction.apply(element) % size);
            bitSet.set(hash, true);
        }
    }

    /**
     * Verifica se um elemento está presente no Bloom Filter.
     * @param element O elemento a ser verificado.
     * @return true se o elemento estiver possivelmente presente, false se estiver definitivamente ausente.
     */
    public synchronized boolean contains(T element) {
        for (Function<T, Integer> hashFunction : hashFunctions) {
            int hash = Math.abs(hashFunction.apply(element) % size);
            if (!bitSet.get(hash)) {
                return false; // Qualquer bit sendo 0 significa que o elemento definitivamente não está no conjunto
            }
        }
        return true; // Possivelmente está no conjunto
    }
}
