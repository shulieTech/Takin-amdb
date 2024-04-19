//package io.shulie.amdb.config;
//
//import io.netty.buffer.ByteBuf;
//import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * @author zhangz
// * Created on 2024/4/11 11:41
// * Email: zz052831@163.com
// */
//@Configuration
//public class UndertowConfig {
//    @Bean
//    public UndertowServletWebServerFactory undertowServletWebServerFactory() {
//        UndertowServletWebServerFactory factory = new UndertowServletWebServerFactory();
//        // 禁用 Undertow 的默认缓冲区，以支持带有特殊字符的 URL
//        factory.addBuilderCustomizers(undertow -> undertow.setByteBufferPool(new CustomByteBufferPool(8192)));
//        factory.addBuilderCustomizers(undertow -> undertow.setServerOption(UndertowOptions.ALLOW_ENCODED_SLASH, true));
//        // 其他 Undertow 配置可以在这里添加
//        return factory;
//    }
//
//    class CustomByteBufferPool extends ByteBufPool {
//        public CustomByteBufferPool(int initialCapacity) {
//            super(initialCapacity);
//        }
//
//        @Override
//        public ByteBuf allocate() {
//            // 根据需要自定义 ByteBuf 分配逻辑
//            return super.allocate();
//        }
//    }
//}
//
//
//
