-- Add gallery images and product video (run on existing DB once)
ALTER TABLE mall_product
  ADD COLUMN album varchar(2000) DEFAULT '' COMMENT '相册图，逗号分隔' AFTER pic,
  ADD COLUMN video varchar(512) DEFAULT '' COMMENT '商品视频URL' AFTER album;
