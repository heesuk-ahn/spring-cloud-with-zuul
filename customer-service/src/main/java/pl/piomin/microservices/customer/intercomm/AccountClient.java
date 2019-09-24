package pl.piomin.microservices.customer.intercomm;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import pl.piomin.microservices.customer.model.Account;

//RestTemplate로 설정을 추가하기는 귀찮기 때문에, FeignClient에 유레카 서버에 등록된 service 이름을 입력하면 된다.
//또 서비스 발견이 true로 되어있어야 한다.
//참고 : https://happyer16.tistory.com/entry/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C-%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%EA%B0%84-%ED%86%B5%EC%8B%A0%EC%9D%B4%EB%9E%80-Ribbon
@FeignClient("account-service")
public interface AccountClient {

    @RequestMapping(method = RequestMethod.GET, value = "/accounts/customer/{customerId}")
    List<Account> getAccounts(@PathVariable("customerId") Integer customerId);
    
}
