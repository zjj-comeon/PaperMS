package com.database.paperms.service.Impl;

import com.database.paperms.entity.ResearchDirection;
import com.database.paperms.mapper.ResearchDirectionMapper;
import com.database.paperms.service.ResearchDirectionService;
import com.database.paperms.utils.TreeNode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ResearchDirectionServiceImpl implements ResearchDirectionService {

    @Resource
    private ResearchDirectionMapper rdMapper;

    @Override
    public int saveResearchDirection(ResearchDirection researchDirection) {
        return rdMapper.insertResearchDirection(researchDirection);
    }

    @Override
    public int deleteResearchDirection(Integer rdId) {
        return rdMapper.deleteResearchDirection(rdId);
    }

    @Override
    public int updateResearchDirection(ResearchDirection researchDirection) {
        return rdMapper.updateResearchDirection(researchDirection);
    }

    @Override
    public List<TreeNode> list() throws NullPointerException {
        List<ResearchDirection> list = rdMapper.list();
        List<TreeNode> lists = new ArrayList<>();
        TreeNode root = null;
        TreeNode tree = null;
        TreeNode leaf;
        try {
            for (ResearchDirection rd : list) {
                switch (rd.getRdId().length()) {
                    case 2:
                        root = new TreeNode(rd.getRdName(), rd.getRdId(), new ArrayList<>());
                        lists.add(root);
                        break;
                    case 4:
                        tree = new TreeNode(rd.getRdName(), rd.getRdId(), new ArrayList<>());
                        Objects.requireNonNull(root).insert(tree);
                        break;
                    case 6:
                        leaf = new TreeNode(rd.getRdName(), rd.getRdId(), null);
                        Objects.requireNonNull(tree).insert(leaf);
                        break;
                }
            }
        } catch (NullPointerException e) {
            throw new NullPointerException();
        }
        return lists;
    }
}
