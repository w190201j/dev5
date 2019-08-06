package com.wander.manifold.service.impl;

import com.wander.manifold.mapper.IQuestionMapper;
import com.wander.manifold.pojo.Question;
import com.wander.manifold.service.IQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by 胥珂铭 on 2019/8/2.
 */
@Service("questionService")
public class QuestionServiceImpl implements IQuestionService {

    @Autowired
    private IQuestionMapper questionMapper;

    @Override
    public Integer addQuestion(Question question) {
        return questionMapper.insert(question);
    }

    @Override
    public Integer addRelation(Long topicId, Long questionId) {
        return questionMapper.insertRelation(topicId,questionId);
    }

    @Override
    public Question queryByTitle(String title) {
        return questionMapper.queryByTitle(title);
    }

    @Override
    public Question queryById(Long questionId) {
        return questionMapper.queryById(questionId);
    }
}
