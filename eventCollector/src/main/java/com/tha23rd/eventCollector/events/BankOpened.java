package com.tha23rd.eventCollector.events;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BankOpened
{
	boolean is_group;
	BankItem[] items;
	int value;
}
