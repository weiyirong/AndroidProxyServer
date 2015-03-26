package com.cfun.proxy.modle;

/**
 * Created by yzq on 15-2-19.
 */
public class ShellResult
{
	private int exitStatu = -1;
	private String errorOutput;
	private String output;

	public ShellResult()
	{
	}

	public ShellResult(int exitStatu)
	{
		this.exitStatu = exitStatu;
	}

	public ShellResult(int exitStatu, String errorOutput, String output)
	{
		this.exitStatu = exitStatu;
		this.errorOutput = errorOutput;
		this.output = output;
	}

	public int getExitStatu()
	{
		return exitStatu;
	}

	public void setExitStatu(int exitStatu)
	{
		this.exitStatu = exitStatu;
	}

	public String getErrorOutput()
	{
		return errorOutput;
	}

	public void setErrorOutput(String errorOutput)
	{
		this.errorOutput = errorOutput;
	}

	public String getOutput()
	{
		return output;
	}

	public void setOutput(String output)
	{
		this.output = output;
	}
}
