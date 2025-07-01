package com.zetarapower.monitor.ui.fragment

/**
 * Created by Evgenii Doronin
 * LinkedIn: https://www.linkedin.com/in/evgeniydoronin
 * 
 * Современный фрагмент настроек с Big Battery дизайном
 */

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.ListPopupWindow.POSITION_PROMPT_BELOW
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.clj.fastble.BleManager
import com.zetarapower.monitor.MainActivity
import com.zetarapower.monitor.R
import com.zetarapower.monitor.logic.SettingsProtocolData
import com.zetarapower.monitor.ui.viewmodel.MainViewModel
import com.zetarapower.monitor.utils.dp2px
import com.zetarapower.monitor.utils.getAppVersionName

/**
 * Современный фрагмент настроек с карточным дизайном
 */
class SettingsFragmentNew : Fragment() {

    private var mainViewModel: MainViewModel? = null

    // UI Elements
    private lateinit var versionNameText: TextView

    // Module ID
    private lateinit var idsContainer: CardView
    private lateinit var selectedIdText: TextView
    private var mIdsPopWindow: ListPopupWindow? = null

    // CAN Protocol
    private lateinit var canContainer: CardView
    private lateinit var selectedCANText: TextView
    private var mCANPopWindow: ListPopupWindow? = null

    // RS485 Protocol
    private lateinit var rs485Container: CardView
    private lateinit var selectedRS485Text: TextView
    private var mRS485PopWindow: ListPopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = activity?.let { ViewModelProviders.of(it)[MainViewModel::class.java] }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_settings_new, container, false)
        
        initViews(root)
        setupClickListeners()
        observeViewModel()
        loadInitialData()
        
        return root
    }

    /**
     * Инициализация view элементов
     */
    private fun initViews(root: View) {
        versionNameText = root.findViewById(R.id.versionName)

        // Module ID
        idsContainer = root.findViewById(R.id.IdsContainer)
        selectedIdText = root.findViewById(R.id.selectedId)

        // CAN Protocol
        canContainer = root.findViewById(R.id.CANContainer)
        selectedCANText = root.findViewById(R.id.canId)

        // RS485 Protocol
        rs485Container = root.findViewById(R.id.RS485Container)
        selectedRS485Text = root.findViewById(R.id.RS485Id)

        // Установка версии приложения
        versionNameText.text = getAppVersionName(requireActivity())
    }

    /**
     * Настройка обработчиков кликов
     */
    private fun setupClickListeners() {
        // Module ID click listener
        if (getString(R.string.settings_module_ID) == "true") {
            selectedIdText.setOnClickListener {
                if (mIdsPopWindow == null) {
                    initIdListPop()
                }
                mIdsPopWindow?.show()
            }
        } else {
            idsContainer.visibility = View.GONE
        }

        // CAN and RS485 click listeners
        if (getString(R.string.settings_module_CAN) == "true") {
            selectedCANText.setOnClickListener {
                if (mCANPopWindow == null) {
                    initCANListPop()
                }
                mCANPopWindow?.show()
            }

            selectedRS485Text.setOnClickListener {
                if (mRS485PopWindow == null) {
                    initRS485ListPop()
                }
                mRS485PopWindow?.show()
            }
        } else {
            canContainer.visibility = View.GONE
            rs485Container.visibility = View.GONE
        }
    }

    /**
     * Подписка на изменения в ViewModel
     */
    private fun observeViewModel() {
        // Selected ID observer
        mainViewModel?.selectedId?.observe(viewLifecycleOwner, Observer<Int> { id ->
            val moduleId = if (id == -1) 1 else id
            selectedIdText.text = "ID$moduleId"
            
            // Disable CAN and RS485 for non-ID1 modules
            if (moduleId != 1) {
                selectedCANText.isEnabled = false
                selectedRS485Text.isEnabled = false
                selectedCANText.alpha = 0.5f
                selectedRS485Text.alpha = 0.5f
            } else {
                selectedCANText.isEnabled = true
                selectedRS485Text.isEnabled = true
                selectedCANText.alpha = 1.0f
                selectedRS485Text.alpha = 1.0f
            }
        })

        // CAN Protocol observer
        mainViewModel?.canData?.observe(viewLifecycleOwner, Observer<SettingsProtocolData> { data ->
            if (data != null) {
                val index = data.selectedIndex
                if (data.protocolArray != null && data.protocolArray.size > index) {
                    selectedCANText.text = data.protocolArray[index]
                }
            }
        })

        // RS485 Protocol observer
        mainViewModel?.rs485Protocol?.observe(viewLifecycleOwner, Observer<SettingsProtocolData> { data ->
            if (data != null) {
                val index = data.selectedIndex
                if (data.protocolArray != null && data.protocolArray.size > index) {
                    selectedRS485Text.text = data.protocolArray[index]
                }
            }
        })
    }

    /**
     * Загрузка начальных данных
     */
    private fun loadInitialData() {
        if (BleManager.getInstance().allConnectedDevice?.isNotEmpty() == true) {
            if (activity != null) {
                (activity as MainActivity).stopTimer()
                (activity as MainActivity).getSettingData()
            }
        }
    }

    /**
     * Инициализация popup для выбора Module ID
     */
    private fun initIdListPop() {
        val lists: List<String> = listOf(
            "ID1", "ID2", "ID3", "ID4", "ID5", "ID6", "ID7", "ID8",
            "ID9", "ID10", "ID11", "ID12", "ID13", "ID14", "ID15", "ID16"
        )
        
        mIdsPopWindow = ListPopupWindow(requireActivity()).apply {
            setListSelector(context?.getDrawable(R.drawable.list_item_selector))
            setBackgroundDrawable(context?.getDrawable(R.drawable.item_bg))
            setAdapter(
                ArrayAdapter<String>(
                    requireContext(),
                    R.layout.popup_list_item,
                    lists
                )
            )
            width = dp2px(100)
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

    /**
     * Инициализация popup для выбора CAN Protocol
     */
    private fun initCANListPop() {
        if (mainViewModel?.canData?.value == null) return
        
        mCANPopWindow = ListPopupWindow(requireActivity()).apply {
            setListSelector(context?.getDrawable(R.drawable.list_item_selector))
            setBackgroundDrawable(context?.getDrawable(R.drawable.item_bg))
            setAdapter(
                ArrayAdapter<String>(
                    requireContext(),
                    R.layout.popup_list_item,
                    mainViewModel?.canData?.value!!.protocolArray
                )
            )
            width = dp2px(150)
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

    /**
     * Инициализация popup для выбора RS485 Protocol
     */
    private fun initRS485ListPop() {
        if (mainViewModel?.rs485Protocol?.value == null) return
        
        mRS485PopWindow = ListPopupWindow(requireActivity()).apply {
            setListSelector(context?.getDrawable(R.drawable.list_item_selector))
            setBackgroundDrawable(context?.getDrawable(R.drawable.item_bg))
            setAdapter(
                ArrayAdapter<String>(
                    requireContext(),
                    R.layout.popup_list_item,
                    mainViewModel?.rs485Protocol?.value!!.protocolArray
                )
            )
            width = dp2px(150)
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

    companion object {
        @JvmStatic
        fun newInstance(): SettingsFragmentNew {
            return SettingsFragmentNew()
        }
    }
}
