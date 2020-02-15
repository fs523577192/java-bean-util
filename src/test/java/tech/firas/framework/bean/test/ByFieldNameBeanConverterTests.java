/*
 * Copyright 2020-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.firas.framework.bean.test;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.convert.converter.Converter;

import tech.firas.framework.bean.ByFieldNameBeanConverter;

public class ByFieldNameBeanConverterTests {

    private static final Random random = new Random();

    @Test
    public void test() throws NoSuchMethodException {
        final Converter<A, B> toBConverter1 = new ByFieldNameBeanConverter<>(A.class, B.class, null);

        final ByFieldNameBeanConverter.Configuration toCConf = new ByFieldNameBeanConverter.Configuration();
        toCConf.getGetterConfiguration().setAllowDirectlyGetField(true);
        toCConf.getSetterConfiguration().setAllowDirectlySetField(true);
        final Converter<A, C> toCConverter = new ByFieldNameBeanConverter<>(A.class, C.class, toCConf);

        for (int i = 65536; i > 0; i -= 1) {
            final ByFieldNameBeanConverter.Configuration toBConf = new ByFieldNameBeanConverter.Configuration();
            toBConf.getGetterConfiguration().setAllowGetBoolean(random.nextBoolean());
            toBConf.getGetterConfiguration().setAllowDirectlyGetField(random.nextBoolean());
            toBConf.getSetterConfiguration().setAllowDirectlySetField(random.nextBoolean());
            final Converter<A, B> toBConverter2 = new ByFieldNameBeanConverter<>(A.class, B.class, toBConf);
            final A a = new A();
            final B b1 = toBConverter1.convert(a);
            final B b2 = toBConverter2.convert(a);

            testB(a, b1, new ByFieldNameBeanConverter.Configuration());
            testB(a, b2, toBConf);

            if (a.getCcCc() != null && a.getEeEe() != null && a.getGgGg() != null &&
                    a.bb != null && a.dd != null && a.ff != null) {
                testC(a, toCConverter.convert(a));
            }
        }
    }

    private static void testB(final A a, final B b, final ByFieldNameBeanConverter.Configuration configuration) {
        Assert.assertNotNull(b);
        Assert.assertEquals(a.isAaAa(), b.isAaAa());
        Assert.assertEquals(a.getCcCc(), b.getCcCc());
        Assert.assertEquals(a.getDdDd(), b.getDdDd());
        Assert.assertEquals(a.getEeEe(), b.getEeEe());
        Assert.assertEquals(a.getFfFf(), b.getFfFf(), 1e-8);
        Assert.assertEquals(a.getGgGg(), b.getGgGg());

        if (configuration.getGetterConfiguration().isAllowGetBoolean() ||
                configuration.getGetterConfiguration().isAllowDirectlyGetField()) {
            Assert.assertEquals(a.getBbBb(), b.isBbBb());
        } else {
            Assert.assertFalse(b.isBbBb());
        }

        if (configuration.getGetterConfiguration().isAllowDirectlyGetField() &&
                configuration.getSetterConfiguration().isAllowDirectlySetField()) {
            Assert.assertEquals(a.aa, b.aa);
            Assert.assertEquals(a.bb, b.bb);
            Assert.assertEquals(a.cc, b.cc);
            Assert.assertEquals(a.dd, b.dd);
            Assert.assertEquals(a.ee, b.ee, 1e-8);
            Assert.assertEquals(a.ff, b.ff);
        } else {
            Assert.assertFalse(b.aa);
            Assert.assertNull(b.bb);
            Assert.assertEquals(0, b.cc);
            Assert.assertNull(b.dd);
            Assert.assertEquals(0.0, b.ee, 1e-8);
            Assert.assertNull(b.ff);
        }
    }

    private static void testC(final A a, final C c) {
        Assert.assertNotNull(c);
        Assert.assertNotNull(c.aaAa);
        Assert.assertNotNull(c.ddDd);
        Assert.assertNotNull(c.ffFf);
        Assert.assertNotNull(c.aa);
        Assert.assertNotNull(c.cc);
        Assert.assertNotNull(c.ee);

        Assert.assertEquals(a.isAaAa(), c.aaAa.booleanValue());
        Assert.assertEquals(a.getCcCc().booleanValue(), c.ccCc);
        Assert.assertEquals(a.getDdDd(), c.ddDd.intValue());
        Assert.assertEquals(a.getEeEe().intValue(), c.eeEe);
        Assert.assertEquals(a.getFfFf(), c.ffFf.doubleValue(), 1e-8);
        Assert.assertEquals(a.getGgGg().doubleValue(), c.ggGg, 1e-8);

        Assert.assertEquals(a.aa, c.aa.booleanValue());
        Assert.assertEquals(a.bb.booleanValue(), c.bb);
        Assert.assertEquals(a.cc, c.cc.intValue());
        Assert.assertEquals(a.dd.intValue(), c.dd);
        Assert.assertEquals(a.ee, c.ee.doubleValue(), 1e-8);
        Assert.assertEquals(a.ff.doubleValue(), c.ff, 1e-8);
    }

    public static final class A {
        private boolean aaAa = random.nextBoolean();
        private boolean bbBb = random.nextBoolean();
        private Boolean ccCc = random.nextBoolean() ? null : random.nextBoolean();
        private int ddDd = random.nextInt();
        private Integer eeEe = random.nextBoolean() ? null : random.nextInt();
        private double ffFf = random.nextDouble();
        private Double ggGg = random.nextBoolean() ? null : random.nextDouble();

        public boolean isAaAa() {
            return aaAa;
        }

        public boolean getBbBb() {
            return bbBb;
        }

        public Boolean getCcCc() {
            return ccCc;
        }

        public int getDdDd() {
            return ddDd;
        }

        public Integer getEeEe() {
            return eeEe;
        }

        public double getFfFf() {
            return ffFf;
        }

        public Double getGgGg() {
            return ggGg;
        }

        private boolean aa = random.nextBoolean();
        private Boolean bb = random.nextBoolean() ? null : random.nextBoolean();
        int cc = random.nextInt();
        Integer dd = random.nextBoolean() ? null : random.nextInt();
        public double ee = random.nextDouble();
        public Double ff = random.nextBoolean() ? null : random.nextDouble();
    }

    public static final class B {
        private boolean aaAa;
        private boolean bbBb;
        private Boolean ccCc;
        private int ddDd;
        private Integer eeEe;
        private double ffFf;
        private Double ggGg;

        public B() {}

        public boolean isAaAa() {
            return aaAa;
        }

        public void setAaAa(final boolean aaAa) {
            this.aaAa = aaAa;
        }

        public boolean isBbBb() {
            return bbBb;
        }

        public void setBbBb(final boolean bbBb) {
            this.bbBb = bbBb;
        }

        public Boolean getCcCc() {
            return ccCc;
        }

        public void setCcCc(final Boolean ccCc) {
            this.ccCc = ccCc;
        }

        public int getDdDd() {
            return ddDd;
        }

        public void setDdDd(final int ddDd) {
            this.ddDd = ddDd;
        }

        public Integer getEeEe() {
            return eeEe;
        }

        public void setEeEe(final Integer eeEe) {
            this.eeEe = eeEe;
        }

        public double getFfFf() {
            return ffFf;
        }

        public void setFfFf(final double ffFf) {
            this.ffFf = ffFf;
        }

        public Double getGgGg() {
            return ggGg;
        }

        public void setGgGg(final Double ggGg) {
            this.ggGg = ggGg;
        }

        private boolean aa;
        private Boolean bb;
        int cc;
        Integer dd;
        public double ee;
        public Double ff;
    }

    public static final class C {
        private Boolean aaAa;
        private boolean ccCc;
        private Integer ddDd;
        private int eeEe;
        private Double ffFf;
        private double ggGg;

        public C() {}

        public void setAaAa(final Boolean aaAa) {
            this.aaAa = aaAa;
        }

        public void setCcCc(final boolean ccCc) {
            this.ccCc = ccCc;
        }

        public void setDdDd(final Integer ddDd) {
            this.ddDd = ddDd;
        }

        public void setEeEe(final int eeEe) {
            this.eeEe = eeEe;
        }

        public void setFfFf(final Double ffFf) {
            this.ffFf = ffFf;
        }

        public void setGgGg(final double ggGg) {
            this.ggGg = ggGg;
        }

        private Boolean aa;
        private boolean bb;
        Integer cc;
        int dd;
        public Double ee;
        public double ff;
    }
}
