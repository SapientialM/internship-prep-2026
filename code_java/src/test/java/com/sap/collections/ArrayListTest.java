package com.sap.collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import static org.junit.Assert.assertEquals;

/**
 *
 *
 * @author SapientialM
 * @date 2026/3/28 19:40
 * @since 1.0
 */
class ArrayListTest {
    @Test
    void operateTest(){
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            list.add(String.valueOf(i));
        }
        int i = 0;
        for(String str : list){
            assertEquals(String.valueOf(i++), str);
        }
        assertEquals(30, list.size());
        list.remove(15);
        list.remove("18");
        assertEquals(28, list.size());
        assertEquals("16", list.get(15));
        assertEquals("24", list.get(22));

    }
}