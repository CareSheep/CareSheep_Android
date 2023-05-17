package com.swu.caresheep.ui.guardian

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.swu.caresheep.databinding.FragmentGuardianHomeBinding

class GuardianHomeFragment : Fragment() {

    private lateinit var binding: FragmentGuardianHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGuardianHomeBinding.inflate(inflater, container, false)

        // 당겨서 새로고침 기능 세팅
        binding.layoutSwipeRefresh.setOnRefreshListener {
//            refreshData()
        }

        // 스크롤 업 대신에 리프레쉬 이벤트가 트리거 되는걸 방지하기 위해서
        // scrollView의 scroll y축이 0, 즉 최상단에 위치했을 때만 refreshLayout을 활성화
        binding.layoutScroll.viewTreeObserver.addOnScrollChangedListener {
            binding.layoutSwipeRefresh.isEnabled = (binding.layoutScroll.scrollY == 0)
        }

        return binding.root
    }

}