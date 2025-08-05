// data/Group.kt - 그룹 생성 API 모델 추가
package com.example.capstone_2.data

// UI에서 사용할 Group 모델
data class Group(
    val id: Int,
    val groupName: String,
    val description: String,
    val createDate: String,
    val modifyDate: String
) {
    // UI에서 필요한 속성들
    val title: String
        get() = groupName

    val memberCount: Int
        get() = 0 // 임시로 0

    val imageResId: Int
        get() = com.example.capstone_2.R.drawable.black
}

// API 응답 모델들
data class GroupApiResponse(
    val message: String,
    val data: GroupData?
)

data class GroupData(
    val groups: List<Group>
)

// === 그룹 생성 관련 모델들 ===

// 그룹 생성 요청 모델
data class CreateGroupRequest(
    val group_name: String,
    val description: String
)

// 그룹 생성 응답 모델
data class CreateGroupResponse(
    val message: String,
    val data: Group?
)