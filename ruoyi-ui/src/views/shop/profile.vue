<template>
  <div class="shop-profile shop-content">
    <h2 class="page-title shop-only-pc">我的资料</h2>
    <el-empty v-if="!isLogin" description="登录后编辑个人资料" class="guest-empty">
      <el-button type="primary" @click="goLogin">去登录</el-button>
    </el-empty>
    <template v-else>
      <div class="profile-card" v-loading="loading">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="昵称" prop="nickName">
            <el-input v-model="form.nickName" placeholder="显示在商城的名称" maxlength="30" />
          </el-form-item>
          <el-form-item label="手机号" prop="phonenumber">
            <el-input v-model="form.phonenumber" placeholder="用于收货联系" maxlength="11" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="选填" maxlength="50" />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="form.sex">
              <el-radio value="0">男</el-radio>
              <el-radio value="1">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="登录账号">
            <el-input :model-value="form.userName" disabled />
          </el-form-item>
        </el-form>
        <el-button type="primary" size="large" class="save-btn" :loading="saving" @click="submit">保存资料</el-button>
      </div>
    </template>
  </div>
</template>

<script setup name="ShopProfile">
import { getUserProfile, updateUserProfile } from '@/api/system/user'
import useUserStore from '@/store/modules/user'

const router = useRouter()
const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const isLogin = computed(() => !!userStore.token)
const loading = ref(true)
const saving = ref(false)
const form = ref({ nickName: '', phonenumber: '', email: '', sex: '0', userName: '' })
const rules = {
  nickName: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phonenumber: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

function goLogin() {
  promptShopLogin(router, '/shop/profile', { scene: 'default' })
}

function loadProfile() {
  if (!userStore.token) {
    loading.value = false
    return
  }
  loading.value = true
  getUserProfile().then(res => {
    const u = res.data || {}
    form.value = {
      nickName: u.nickName,
      phonenumber: u.phonenumber,
      email: u.email || '',
      sex: u.sex || '0',
      userName: u.userName
    }
    loading.value = false
  }).catch(() => { loading.value = false })
}

function submit() {
  proxy.$refs.formRef.validate(valid => {
    if (!valid) return
    saving.value = true
    updateUserProfile({
      nickName: form.value.nickName,
      phonenumber: form.value.phonenumber,
      email: form.value.email,
      sex: form.value.sex
    }).then(() => {
      proxy.$modal.msgSuccess('保存成功')
      return userStore.getInfo()
    }).finally(() => { saving.value = false })
  })
}

onMounted(loadProfile)
</script>

<style scoped lang="scss">
.shop-profile { padding: 12px 0 24px; }
.page-title { margin: 0 0 16px; font-size: 22px; color: #333; }
.profile-card {
  background: #fff; border-radius: 12px; padding: 16px;
}
.save-btn { width: 100%; margin-top: 8px; }
.guest-empty { padding: 48px 16px; }
@media (min-width: 769px) {
  .shop-profile { padding: 24px 0 40px; }
  .profile-card {
    max-width: 640px;
    padding: 32px;
    border-radius: 8px;
  }
  .save-btn { width: auto; min-width: 160px; }
}
</style>
