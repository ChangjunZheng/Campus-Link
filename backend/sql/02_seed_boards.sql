-- 6 个固定版块种子数据（PRD F-FORUM-001；Q1 版块终稿在需求评审确认）
USE campuslink;

INSERT INTO boards (code, name, description, type, sort) VALUES
('qna',       '技术问答', '提问、报错排查、环境配置、技术选型', 'QUESTION',   1),
('resources', '学习资源', '教程、笔记、工具、资源分享',         'DISCUSSION', 2),
('interview', '面经求职', '实习 / 校招面经、内推、职业规划',     'DISCUSSION', 3),
('contest',   '竞赛交流', 'ACM / 蓝桥杯 / 数模、组队、真题',     'DISCUSSION', 4),
('course',    '课程交流', '课程攻略、作业讨论、考试经验',         'DISCUSSION', 5),
('chat',      '闲聊灌水', '校园生活、轻松话题',                  'DISCUSSION', 6);
