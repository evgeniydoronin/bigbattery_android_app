package com.zetarapower.monitor.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.ListPopupWindow.POSITION_PROMPT_BELOW
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.clj.fastble.BleManager
import com.clj.fastble.utils.HexUtil
import com.zetarapower.monitor.MainActivity
import com.zetarapower.monitor.R
import com.zetarapower.monitor.logic.SettingsProtocolData
import com.zetarapower.monitor.ui.viewmodel.MainViewModel
import com.zetarapower.monitor.utils.dp2px
import com.zetarapower.monitor.utils.getAppVersionName

class SettingsFragment : Fragment() {

    private var mainViewModel: MainViewModel? = null

    private lateinit var versionNameText: TextView

    private lateinit var idsContainer: View
    private lateinit var selectedIdText: TextView
    private var mIdsPopWindow: ListPopupWindow? = null

    private lateinit var canContainer: View
    private lateinit var selectedCANText: TextView
    private var mCANPopWindow: ListPopupWindow? = null

    private lateinit var rs485Container: View
    private lateinit var selectedRS485Text: TextView
    private var mRS485PopWindow: ListPopupWindow? = null

    // private var handler: Handler = Handler(Looper.getMainLooper())

    /**
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = activity?.let { ViewModelProviders.of(it)[MainViewModel::class.java] }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        versionNameText = root.findViewById(R.id.versionName)

        // IDS
        idsContainer = root.findViewById(R.id.IdsContainer)
        selectedIdText = root.findViewById(R.id.selectedId)

        if (getString(R.string.settings_module_ID) == "true") {
            selectedIdText.setOnClickListener(View.OnClickListener {
                if (mIdsPopWindow == null) {
                    initIdListPop()
                }
                mIdsPopWindow?.show()
            })
        } else {
            idsContainer.visibility = View.GONE
        }


        // CAN
        canContainer = root.findViewById(R.id.CANContainer)
        selectedCANText = root.findViewById(R.id.canId)

        // RS485
        rs485Container = root.findViewById(R.id.RS485Container)
        selectedRS485Text = root.findViewById(R.id.RS485Id)

        if (getString(R.string.settings_module_CAN) == "true") {
            selectedCANText.setOnClickListener(View.OnClickListener {
                if (mCANPopWindow == null) {
                    initCANListPop()
                }
                mCANPopWindow?.show()
            })

            selectedRS485Text.setOnClickListener(View.OnClickListener {
                if (mRS485PopWindow == null) {
                    initRS485ListPop()
                }
                mRS485PopWindow?.show()
            })
        } else {
            canContainer.visibility = View.GONE
            rs485Container.visibility = View.GONE
        }


        versionNameText.text = getAppVersionName(requireActivity())

//        handler.postDelayed(Runnable {
//            mainViewModel?.handleResponseData(HexUtil.hexStringToBytes("100470080B5030312D4752570000005030322D534C4B0000005030332D4459000000005030342D4D47520000005030352D5643540000005030362D4C55580000005030372D534D410000005030382D494E480000005030392D534F4C0000005031302D41464F0000005031312D535455000000350B"))
//        }, 250)
//
//        handler.postDelayed(Runnable {
//            mainViewModel?.handleResponseData(HexUtil.hexStringToBytes("10033400055030312D4752570000005030322D4C55580000005030332D5343480000005030342D494E480000005030352D564F4C000000BCD8"))
//        }, 500)

        if (BleManager.getInstance().allConnectedDevice?.isNotEmpty() == true) {
            if (activity != null) {
                (activity as MainActivity).stopTimer()
                (activity as MainActivity).getSettingData()
            }
        }

        mainViewModel?.selectedId?.observe(viewLifecycleOwner, Observer<Int> {
            var id = if (it == -1) 1 else it
            selectedIdText.text = "ID$id  "
            if (id != 1) {
                selectedCANText.isEnabled = false
                selectedRS485Text.isEnabled = false
            } else {
                selectedCANText.isEnabled = true
                selectedRS485Text.isEnabled = true
            }
        })

        mainViewModel?.canData?.observe(viewLifecycleOwner, Observer<SettingsProtocolData> {
            if (it != null) {
                var index = it.selectedIndex
                if (it.protocolArray != null && it.protocolArray.size > index) {
                    selectedCANText.text = it.protocolArray[index] + "  "
                }
            }
        })

        mainViewModel?.rs485Protocol?.observe(viewLifecycleOwner, Observer<SettingsProtocolData> {
            if (it != null) {
                var index = it.selectedIndex
                if (it.protocolArray != null && it.protocolArray.size > index) {
                    selectedRS485Text.text = it.protocolArray[index] + "  "
                }
            }
        })
        return root
    }

    private fun initIdListPop() {
        var lists: List<String> = listOf(
            "ID1",
            "ID2",
            "ID3",
            "ID4",
            "ID5",
            "ID6",
            "ID7",
            "ID8",
            "ID9",
            "ID10",
            "ID11",
            "ID12",
            "ID13",
            "ID14",
            "ID15",
            "ID16"
        )
        mIdsPopWindow = ListPopupWindow(requireActivity())
        mIdsPopWindow?.apply {
            setListSelector(context?.getDrawable(R.drawable.list_item_selector))
            setBackgroundDrawable(context?.getDrawable(R.drawable.item_bg))
            setAdapter(
                ArrayAdapter<String>(
                    requireContext(),
                    R.layout.popup_list_item,
                    lists
                )
            )
            width = dp2px(80)
            height = dp2px(350)
            anchorView = selectedIdText
            isModal = true
            promptPosition = POSITION_PROMPT_BELOW
            setOnItemClickListener { _, _, position, _ ->
                if (BleManager.getInstance().allConnectedDevice != null
                    && BleManager.getInstance().allConnectedDevice.isNotEmpty()
                ) {
                    mainViewModel?.setBMSModuleIdData(
                        BleManager.getInstance().allConnectedDevice[0],
                        null,
                        position + 1
                    )
                }
                mIdsPopWindow?.dismiss()
            }
        }
    }


    private fun initCANListPop() {
        if (mainViewModel?.canData?.value == null) return
        mCANPopWindow = ListPopupWindow(requireActivity())
        mCANPopWindow?.apply {
            setListSelector(context?.getDrawable(R.drawable.list_item_selector))
            setBackgroundDrawable(context?.getDrawable(R.drawable.item_bg))
            setAdapter(
                ArrayAdapter<String>(
                    requireContext(),
                    R.layout.popup_list_item,
                    mainViewModel?.canData?.value!!.protocolArray
                )
            )
            width = dp2px(120)
            height = dp2px(350)
            anchorView = selectedCANText
            isModal = true
            promptPosition = POSITION_PROMPT_BELOW
            setOnItemClickListener { _, _, position, _ ->
                if (BleManager.getInstance().allConnectedDevice != null
                    && BleManager.getInstance().allConnectedDevice.isNotEmpty()
                ) {
                    mainViewModel?.setCanData(
                        BleManager.getInstance().allConnectedDevice[0],
                        null,
                        position
                    )
                }
                mCANPopWindow?.dismiss()
            }
        }
    }

    private fun initRS485ListPop() {
        if (mainViewModel?.rs485Protocol?.value == null) return
        mRS485PopWindow = ListPopupWindow(requireActivity())
        mRS485PopWindow?.apply {
            setListSelector(context?.getDrawable(R.drawable.list_item_selector))
            setBackgroundDrawable(context?.getDrawable(R.drawable.item_bg))
            setAdapter(
                ArrayAdapter<String>(
                    requireContext(),
                    R.layout.popup_list_item,
                    mainViewModel?.rs485Protocol?.value!!.protocolArray
                )
            )
            width = dp2px(120)
            height = dp2px(250)
            anchorView = selectedRS485Text
            isModal = true
            promptPosition = POSITION_PROMPT_BELOW
            setOnItemClickListener { _, _, position, _ ->
                if (BleManager.getInstance().allConnectedDevice != null
                    && BleManager.getInstance().allConnectedDevice.isNotEmpty()
                ) {
                    mainViewModel?.setRS485Data(
                        BleManager.getInstance().allConnectedDevice[0],
                        null,
                        position
                    )
                }
                mRS485PopWindow?.dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (BleManager.getInstance().allConnectedDevice?.isNotEmpty() == true) {
            if (activity != null) {
                (activity as MainActivity).getMainBMSData()
                (activity as MainActivity).startTimer()
            }
        }
    }

}