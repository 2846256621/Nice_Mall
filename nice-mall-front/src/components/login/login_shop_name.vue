<template id="login">
    <div>
        <form class="form-inline" @submit.prevent="onSubmit">
            <label>
                <span class="iconfont icon-yonghuming"></span>
                <input type="text" name="username"  value="" placeholder="请输入用户名" class="form-control" v-model="username">
            </label>
            <label>
                <span class="iconfont icon-querenmima"></span>
                <input type="password" name="password" value="" placeholder="请输入您的密码" class="form-control" v-model="password">
            </label>
            <div id="footer">
                <div class="other-login">
                    <span><router-link to="/login_sign/login_shop_phone">手机号登录</router-link></span>
                </div>
              <div class="sign">
                <router-link to="/login_sign/forget" id="forget-password">忘记密码?</router-link>
                <div class="link-to">
                  <button class="btn" @click="handleLogin">登录</button>
                  <router-link to="/login_sign/signup" class="btn" style="margin-left:20px;">注册</router-link>
                </div>
              </div>

            </div>
        </form>
    </div>
</template>
<script>
    export  default {
       inject: ['reload'],
        data(){
            return{
                username:'',
                password:''
            }
        },
        methods:{
            onSubmit(){return false;},
            handleLogin(){
                let datas ={
                    username: this.username,
                    password: this.password
                };
                if(this.username === '' || this.password === ''){
                    alert('请填入完整信息');
                    return;
                }
                else{
                    let that =this;
                    this.$http.post('/login/name',datas).then(res => {                   //请求成功后的处理函数
                        that.$message({
                            message:res.data.message,
                            type:'success',
                            duration:1500
                        });
                        if(res.data.status === true){
                            //检测token
                            window.localStorage["token"] = res.data.data.token;
                            window.localStorage["userId"] = res.data.data.userid;
                            window.localStorage["username"] = res.data.data.username;
                            window.localStorage['isshop'] = res.data.data.isshop;
                            window.localStorage['logintime'] = new Date().getTime();
                            window.localStorage['userAvatar']= res.data.data.userAvatar;
                            this.$router.push({path: '/shop_home/shop_index'});
                            window.location.reload();
                        }
                        else {
                            return;
                        }
                    }).catch(err => {                 //请求失败后的处理函数
                      this.$message.error("登录失败")
                    })
                }

            }
        }
    }
</script>

<style>

</style>
