/*
 * Copyright 2016-2017 Shanghai Boyuan IT Services Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.boyuanitsm.fort.service;

import com.boyuanitsm.fort.bean.enumeration.OnUpdateSecurityResourceOption;
import com.boyuanitsm.fort.domain.SecurityApp;
import com.boyuanitsm.fort.domain.SecurityGroup;
import com.boyuanitsm.fort.domain.SecurityRole;
import com.boyuanitsm.fort.repository.SecurityRoleRepository;
import com.boyuanitsm.fort.repository.search.SecurityRoleSearchRepository;
import com.boyuanitsm.fort.service.util.QueryBuilderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

import static com.boyuanitsm.fort.bean.enumeration.OnUpdateSecurityResourceClass.SECURITY_ROLE;
import static com.boyuanitsm.fort.bean.enumeration.OnUpdateSecurityResourceOption.*;

/**
 * Service Implementation for managing SecurityRole.
 */
@Service
@Transactional
public class SecurityRoleService {

    private final Logger log = LoggerFactory.getLogger(SecurityRoleService.class);

    @Inject
    private SecurityRoleRepository securityRoleRepository;

    @Inject
    private SecurityRoleSearchRepository securityRoleSearchRepository;

    @Inject
    private SecurityResourceUpdateService updateService;

    @Inject
    private SecurityAppService securityAppService;

    /**
     * Save a securityRole.
     *
     * @param securityRole the entity to save
     * @return the persisted entity
     */
    public SecurityRole save(SecurityRole securityRole) {
        log.debug("Request to save SecurityRole : {}", securityRole);

        OnUpdateSecurityResourceOption option = securityRole == null ? POST : PUT;

        SecurityApp app = securityAppService.findCurrentSecurityApp();
        // set app
        if (app != null) {
            securityRole.setApp(app);
        }

        SecurityRole result = securityRoleRepository.save(securityRole);
        securityRoleSearchRepository.save(result);

        updateService.send(option, SECURITY_ROLE, result);
        return result;
    }

    /**
     * Get all the securityRoles.
     *
     * @param pageable the pagination information
     * @param appId
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<SecurityRole> findAll(Pageable pageable, Long appId) {
        log.debug("Request to get all SecurityRoles");
        if (appId != null) {
            return securityRoleRepository.findAllByAppId(pageable, appId);
        } else {
            return securityRoleRepository.findOwnAll(pageable);
        }
    }

    /**
     * Get one securityRole by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public SecurityRole findOne(Long id) {
        log.debug("Request to get SecurityRole : {}", id);
        SecurityRole securityRole = securityRoleRepository.findOneWithEagerRelationships(id);
        return securityRole;
    }

    /**
     * Delete the  securityRole by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete SecurityRole : {}", id);
        String appKey = securityRoleRepository.getOne(id).getApp().getAppKey();

        securityRoleRepository.delete(id);
        securityRoleSearchRepository.delete(id);

        updateService.send(DELETE, SECURITY_ROLE, new SecurityRole(id, appKey));
    }

    /**
     * Search for the securityRole corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<SecurityRole> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of SecurityRoles for query {}", query);
        return securityRoleSearchRepository.search(QueryBuilderUtil.build(query), pageable);
    }

    /**
     * Find this app all roles with eager relationships.
     *
     * @param appKey the appKey of the SecurityApp
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<SecurityRole> findAllByAppKeyWithEagerRelationships(String appKey) {
        return securityRoleRepository.findAllByAppKeyWithEagerRelationships(appKey);
    }

    public SecurityRole findByAppAndName(SecurityApp app, String name) {
        return securityRoleRepository.findByAppAndName(app, name);
    }
}
