## spring cloud를 이용한 마이크로 서비스 샘플

해당 코드는 git에 공유 되어있는 샘플 오픈소스를 따라 학습하면서 문서 해석을 하고, 또 알게된 것들을 정리하고자 한다.

직접 코드를 보거나 문서를 보려면 아래 링크를 참조 하면 된다.

```aidl
https://piotrminkowski.wordpress.com/2017/02/05/part-1-creating-microservice-using-spring-cloud-eureka-and-zuul/
```

이 프로젝트에는 `account-service`와 `customer-service` 두 스프링 어플리케이션이 있다.

이 두 서비스는 각자 다른 리모트 환경에 있으며, 통신은 `Feign Client`를 통해 진행된다.


```
@FeignClient("account-service")
public interface AccountClient {

    @RequestMapping(method = RequestMethod.GET, value = "/accounts/customer/{customerId}")
    List<Account> getAccounts(@PathVariable("customerId") Integer customerId);
    
}
```

또한 Feign Client를 사용하기 위해서는 customer 애플리케이션 기동시에 `@EnableFeignClients`을 달아주어야 한다.


```
package pl.piomin.microservices.customer;
 
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
 
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class Application {
 
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

또 customer-service에 application.yaml에는 중요한 설정 또한 있다.

리본 로드밸런서를 활성화 시켜야 하며, 그리고 Eureka Client가 Eureka Server에

health check을 위해 보내는 reneval 설정과 서비스가 셧다운 되었을 때, 서비스 디스커버리에서 

제외 되도록 설정을 활성화 해야한다.


```aidl
server:
    port: ${PORT:3333}
 
eureka:
    client:
        serviceUrl:
            defaultZone: ${vcap.services.discovery-service.credentials.uri:http://127.0.0.1:8761}/eureka/
    instance:
        leaseRenewalIntervalInSeconds: 1
        leaseExpirationDurationInSeconds: 2
 
ribbon:
    eureka:
        enabled: true
```

위와 같이 application.yaml을 추가하고 나면 이제 우리는 2가지 서비스를 만들었고, 설정도 완료했다.

그러나, 첫번째로 우리는 먼저 `Service Discovery`를 위한 Eureka Server를 만들어야 한다.

`Eureka Server`는 `spring-cloud-starter-eureka-server`로 종속성 추가가 가능하며,

그리고 이를 활성화 시키기 위해서는 Main 클래스에서 `@EnableEurekaServer` 어노테이션을 달아야 한다.

```aidl

server:
    port: ${PORT:8761}
 
eureka:
    instance:
        hostname: localhost
    client:
        registerWithEureka: false
        fetchRegistry: false
    server:
        enableSelfPreservation: false

```

디스커버리 서비스를 가동 후에는 우리는 웹 콘솔에서 직접 확인해볼 수 있다,

디스커버리 서비스 콘솔에 직접 접속해서 확인해보면, 우리는 `customer service`와 `account service`가 동작하고 있는 것을 볼 수 있다.

하지만, 우리는 우리의 내부 서비스들의 복잡성을 외부에는 숨길 필요가 있다. 이것은 하나의 IP:PORT 만 요청을 보내는 클라이언트에게 노출시키는 것이다.

만약 이렇게 하지 않는다면 앞으로 수많은 서비스, 그리고 서비스들의 인스턴스들을 모두 client 측에서 IP:PORT set을 유지하는 것은 어려울 것이다.

물론, 예전에는 가능했다. 네트워크 트래픽이 많지 않았기 때문이다. 하지만 이제는 `대규모 트래픽`이 발생하기 시작하면서 그것은 어려워지게 되었다,

이러한 외부세계에 간단한 하나의 도메인만 노출시키기 위해서 우리를 도와줄 수 있는 것이 바로 게이트웨이 `zuul`이다.

`zuul`은 우리의 요청을 proxy configuration에 맞추어서 알맞은 서비스로 라우팅을 해준다. 이러한 로드 밸런싱은 또한 `Ribbon`에 의해서 이루어질 수 도

있다. `zuul`을 enable 하기 위해서는 `spring-cloud-starter-zuul` 디펜던시를 추가해야 한다. 그리고 나서 `EnableZuulProxy` 어노테이션을

추가하자.

 zuul의 yaml 설정은 아래와 같다.
 
```
server:
    port: 8765
 
zuul:
    prefix: /api
    routes:
        account:
            path: /account/**
            serviceId: account-service
        customer:
            path: /customer/**
            serviceId: customer-service 
```

zuul의 설정을 보면 알 수 있겠지만, zuul gateway는 8765 포트에 바인딩되며, `/api/account/*` 들은 `account-service`로 바인딩 되고,

`/api/customer/*` 들은 `customer-service`로 바인딩 되는 것을 알 수 있다.

 