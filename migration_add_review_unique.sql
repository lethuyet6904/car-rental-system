-- Mỗi đơn hàng chỉ được đánh giá một lần
ALTER TABLE Review
ADD CONSTRAINT uq_review_order UNIQUE (orderId);
