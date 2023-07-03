<template>
    <div id="orders">
        <hr>
        <el-table
            v-bind:data="orders"
            stripe:true
            style="width: 100%">
            <!-- <el-table-column
                prop="id"
                label="序号"
                width="60">
            </el-table-column> -->
            <el-table-column
                prop="title"
                label="订单名称"
                width="180">
            </el-table-column>
            <el-table-column
                prop="orderNo"
                label="订单编号">
            </el-table-column>
            <el-table-column
                prop="orderStatus" 
                label="订单状态">
                <template slot-scope="scope">
                    <el-tag v-if="scope.row.orderStatus === '未支付'">
                    {{scope.row.orderStatus}}
                    </el-tag>
                    <el-tag v-if="scope.row.orderStatus === '支付成功'" type="success">
                    {{ scope.row.orderStatus }}
                    </el-tag>
                    <el-tag v-if="scope.row.orderStatus === '超时已关闭'" type="warning">
                    {{scope.row.orderStatus}}
                    </el-tag>
                    <el-tag v-if="scope.row.orderStatus === '用户已取消'" type="info">
                    {{scope.row.orderStatus}}
                    </el-tag>
                    <el-tag v-if="scope.row.orderStatus === '退款中'" type="danger">
                    {{scope.row.orderStatus}}
                    </el-tag>
                    <el-tag v-if="scope.row.orderStatus === '已退款'" type="info">
                    {{scope.row.orderStatus}}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column
                prop="totalFee"
                label="付款金额/元">
                <template slot-scope="scope">
                    {{scope.row.totalFee / 100}}
                </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
                <template slot-scope="scope">
                    <el-button v-if="scope.row.orderStatus === '未支付'" type="text" @click="cancel(scope.row.orderNo)">取消</el-button>
                    <el-button v-if="scope.row.orderStatus === '支付成功'" type="text" @click="refund(scope.row.orderNo)">退款</el-button>
                </template>
            </el-table-column>
        </el-table>
        <!-- 退款对话框 -->
        <el-dialog
            :visible.sync="refundDialogVisible"
            @close="closeDialog"
            width="350px"
            center>
            <el-form>
                <el-form-item label="退款原因">
                <el-select v-model="reason" placeholder="请选择退款原因">
                    <el-option label="不喜欢" value="不喜欢"></el-option>
                    <el-option label="买错了" value="买错了"></el-option>
                </el-select>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button type="primary" @click="toRefund()" :disabled="refundSubmitBtnDisabled">确 定</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
    import axios from 'axios'

    export default {
        name:'Orders',
        data(){
            return{
                orders:[],                      // 订单列表
                refundDialogVisible: false,     // 退款弹窗
                orderNo: '',                    // 退款订单号
                reason: '',                     // 退款原因,
                refundSubmitBtnDisabled: false, // 防止重复提交

            }
        },
        created(){
            this.showOrders()
        },
        methods:{
            // 数据
            showOrders(){
                axios.get("http://localhost:7290/api/order-info/list").then(
                    Response=>{
                        // 订单列表
                        console.log("请求成功",Response.data)
                        this.orders = Response.data.data.orders
                    },
                    Error=>{
                        console.log("请求失败")
                    }
                )
            },
            // 取消订单
            cancel(orderNo){
                axios.post("http://localhost:7290/api/wx-pay/cancel/" + orderNo).then(
                    Response=>{
                        // 取消订单弹窗通知
                        this.$message.success(Response.data.message)
                        // 刷新订单列表
                        console.log("取消订单后刷新数据。。。。。")
                        this.showOrders()
                    },
                    Error=>{
                        console.log("取消订单失败")
                    }
                )

            },
            // 退款对话框
            refund(orderNo){
                this.refundDialogVisible = true
                this.orderNo = orderNo
                console.log('退款订单',this.orderNo)
            },
            // 关闭退款对话框
            closeDialog(){
                console.log('close......')
                this.refundDialogVisible = false
                // 还原组件状态
                this.orderNo = ''
                this.reason = ''
                this.refundSubmitBtnDisabled = false
            },
            // 退款
            toRefund(){
                this.refundSubmitBtnDisabled = true //禁用按钮，防止重复提交

                var params = new URLSearchParams()
                params.append('orderNo', this.orderNo)
                params.append('reason', this.reason)

                axios.post("http://localhost:7290/api/wx-pay/refund", params).then(
                    Response=>{
                        // 退款结果
                        console.log('response', Response)
                        // 关闭退款弹窗
                        this.closeDialog()
                        // 刷新订单数据 
                        console.log("退款后刷新数据。。。。。")
                        this.showOrders()

                    },
                    Error=>{
                        console.log("退款失败")
                    }
                )


            }
        }

    }
</script>

<style lang="css" scoped>

</style>