package antistatic.spinnerwheel;

    /**
     *可见item的范围
     */
    public class ItemsRange {
        //第一个item
        private int first;
        
        //item的个数
        private int count;
        public ItemsRange() {
            this(0, 0);
        }
        public ItemsRange(int first, int count) {
            this.first = first;
            this.count = count;
        }
        public int getFirst() {
            return first;
        }
        
        public int getLast() {
            return getFirst() + getCount() - 1;
        }
        
        public int getCount() {
            return count;
        }
        
        /**
         * index索引的item是否在可见范围
         * @param index item的索引
         */
        public boolean contains(int index) {
            return index >= getFirst() && index <= getLast();
        }
}