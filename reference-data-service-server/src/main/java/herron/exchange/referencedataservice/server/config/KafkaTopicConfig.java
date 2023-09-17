package herron.exchange.referencedataservice.server.config;

import com.herron.exchange.common.api.common.enums.KafkaTopicEnum;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;


@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic auditTrailTopic() {
        return TopicBuilder
                .name(KafkaTopicEnum.HERRON_REFERENCE_DATA.getTopicName())
                .partitions(1)
                .build();
    }
}
