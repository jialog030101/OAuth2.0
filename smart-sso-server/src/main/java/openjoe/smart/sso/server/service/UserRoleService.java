package openjoe.smart.sso.server.service;

import openjoe.smart.sso.server.stage.mybatisplus.service.BaseService;
import openjoe.smart.sso.server.entity.UserRole;

import java.util.Collection;
import java.util.List;

/**
 * 用户角色映射服务接口
 * 
 * @author Joe
 */
public interface UserRoleService extends BaseService<UserRole> {
	
	/**
     * 根据用户ID给用户分配角色
     * @param userId 用户ID
     * @param roleIdList 角色ID集合
     * @return
     */
    void allocate(Long userId, List<Long> roleIdList);
	
	/**
	 * 根据角色ID集合删除映射
	 * @param idList 角色ID集合
	 * @return
	 */
	void deleteByRoleIds(Collection<Long> idList);
	
	/**
	 * 根据用户ID集合删除映射
	 * @param idList 用户ID集合
	 * @return
	 */
	void deleteByUserIds(Collection<Long> idList);
	
	/**
     * 根据用户ID查角色ID集合
     * @param userId
     * @return
     */
    List<Long> findRoleIdListByUserId(Long userId);
}
