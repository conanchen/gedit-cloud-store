package com.github.conanchen.gedit.store;

import com.github.conanchen.gedit.store.model.StoreProfile;
import com.github.conanchen.gedit.store.profile.grpc.StoreProfileResponse;
import com.github.conanchen.gedit.store.repository.StoreProfileRepository;
import com.github.conanchen.gedit.store.repository.page.OffsetBasedPageRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringDataJpaTest {
    private static final Gson gson = new GsonBuilder().setDateFormat(DateFormat.MILLISECOND_FIELD).create();
    @Resource
    private StoreProfileRepository repository;
    @Before
    public void before(){
        List<StoreProfile> list = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            list.add(StoreProfile.builder()
                    .name( String.valueOf("name"+ System.currentTimeMillis() + i))
                    .type(String.valueOf(i % 2 == 0 ? 0 : 1))
                    .build());

        }
        repository.save(list);
    }

    @Test
    public void pageTest(){
        slice(0,5,"0");
        slice(4,5,"0");
        slice(9,5,"0");
        slice(14,5,"0");

        slice(0,100,"0");
    }

    @Test
    public void existsTest(){
        repository.existsByNameAndOwnerIdNotIn("name15155856292940","ff80818160dff1620160dff16c7d0000");
    }

    private void slice(int from,int size,String type){
        int tempForm = from == 0 ? 0 : from + 1;
        Pageable pageable = new OffsetBasedPageRequest(tempForm,size,new Sort(Sort.Direction.ASC,"createdDate"));
        List<StoreProfile> list;
        if (!StringUtils.isEmpty(type)) {
            list = repository.findByType(type,pageable);
        }else{
            list = repository.findAll(pageable).getContent();
        }
        for (StoreProfile storeProfile : list){
           System.out.println(String.format("current from:[%s]", tempForm++));
           System.out.println(String.format("current row value %s",gson.toJson(storeProfile)));
        }
    }
}
